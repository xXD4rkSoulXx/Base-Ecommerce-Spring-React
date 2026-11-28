import { mkdirSync, writeFileSync } from 'node:fs'
import { join } from 'node:path'
import { PRODUCTS } from './catalog.mjs'
import { IMAGE_COLORS, renderProductImage } from './images.mjs'

const args = process.argv.slice(2)
const flag = (name) => args.includes(`--${name}`)
const option = (name) => args.find((arg) => arg.startsWith(`--${name}=`))?.split('=')[1]

const DRY_RUN = flag('dry-run')
const PREVIEW = flag('preview')
const SKIP_IMAGES = flag('skip-images')
const LIMIT = option('limit') ? Number(option('limit')) : PRODUCTS.length
const STOCK = option('stock') ? Number(option('stock')) : 25
const RETRIES = 5

const API_URL = (process.env.SEED_API_URL ?? '').replace(/\/+$/, '')
const EMAIL = process.env.SEED_ADMIN_EMAIL ?? ''
const PASSWORD = process.env.SEED_ADMIN_PASSWORD ?? ''

const selected = PRODUCTS.slice(0, LIMIT)
const imageCache = new Map()

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

function imageFor(shape, color) {
  const key = `${shape}:${color}`
  if (!imageCache.has(key)) {
    const rendered = IMAGE_COLORS.includes(color) ? renderProductImage(shape, color) : null
    imageCache.set(key, rendered)
  }
  return imageCache.get(key)
}

function expandSkus(product) {
  return product.colors.flatMap((color) => product.sizes.map((size) => ({ color, size })))
}

function preview() {
  const dir = join(process.cwd(), 'preview')
  mkdirSync(dir, { recursive: true })
  const seen = new Set()
  for (const product of selected) {
    for (const color of product.colors) {
      const key = `${product.shape}-${color}`
      if (seen.has(key)) {
        continue
      }
      seen.add(key)
      writeFileSync(join(dir, `${key.toLowerCase()}.png`), imageFor(product.shape, color))
    }
  }
  console.log(`${seen.size} images written to ${dir}`)
}

function summary() {
  const skus = selected.reduce((sum, product) => sum + expandSkus(product).length, 0)
  console.log(`${selected.length} products, ${skus} variants`)
  const byCategory = {}
  for (const product of selected) {
    byCategory[product.category] = (byCategory[product.category] ?? 0) + 1
  }
  console.log(byCategory)
}

class ApiClient {
  constructor(baseUrl) {
    this.baseUrl = baseUrl
    this.cookies = new Map()
    this.accessToken = null
  }

  storeCookies(response) {
    for (const line of response.headers.getSetCookie()) {
      const [pair, ...attributes] = line.split(';').map((part) => part.trim())
      const index = pair.indexOf('=')
      const name = pair.slice(0, index)
      const value = pair.slice(index + 1)
      const path = attributes.find((a) => a.toLowerCase().startsWith('path='))?.slice(5) ?? '/'
      const maxAge = attributes.find((a) => a.toLowerCase().startsWith('max-age='))?.slice(8)
      if (value === '' || maxAge === '0') {
        this.cookies.delete(name)
      } else {
        this.cookies.set(name, { value, path })
      }
    }
  }

  cookieHeader(path) {
    return [...this.cookies.entries()]
      .filter(([, cookie]) => path.startsWith(cookie.path))
      .map(([name, cookie]) => `${name}=${cookie.value}`)
      .join('; ')
  }

  async raw(method, path, { json, form } = {}) {
    const headers = { Accept: 'application/json', 'X-Requested-With': 'XMLHttpRequest' }
    const cookie = this.cookieHeader(path)
    if (cookie) {
      headers.Cookie = cookie
    }
    if (this.accessToken) {
      headers.Authorization = `Bearer ${this.accessToken}`
    }
    const xsrf = this.cookies.get('XSRF-TOKEN')
    if (xsrf && method !== 'GET') {
      headers['X-XSRF-TOKEN'] = decodeURIComponent(xsrf.value)
    }
    let body
    if (json !== undefined) {
      headers['Content-Type'] = 'application/json'
      body = JSON.stringify(json)
    } else if (form) {
      body = form()
    }
    const response = await fetch(`${this.baseUrl}${path}`, { method, headers, body })
    this.storeCookies(response)
    return response
  }

  async request(method, path, options = {}) {
    let refreshed = false
    let csrfRetried = false
    for (let attempt = 1; ; attempt++) {
      let response
      try {
        response = await this.raw(method, path, options)
      } catch (error) {
        if (attempt >= RETRIES) {
          throw new Error(`${method} ${path}: ${error.cause?.code ?? error.message}`)
        }
        await sleep(attempt * 4000)
        continue
      }

      if ([502, 503, 504].includes(response.status) && attempt < RETRIES) {
        await sleep(attempt * 4000)
        continue
      }
      if (response.status === 401 && !refreshed && path !== '/api/auth/login') {
        refreshed = true
        await this.refresh()
        continue
      }
      if (response.status === 403 && !csrfRetried && method !== 'GET') {
        csrfRetried = true
        await this.raw('GET', '/api/products/featured')
        continue
      }

      const text = await response.text()
      const data = text ? safeParse(text) : null
      if (!response.ok) {
        const detail = typeof data === 'object' && data ? (data.message ?? JSON.stringify(data)) : text
        const error = new Error(`${method} ${path} -> ${response.status} ${detail}`)
        error.status = response.status
        throw error
      }
      return data
    }
  }

  async login() {
    await this.raw('GET', '/api/products/featured')
    const data = await this.request('POST', '/api/auth/login', { json: { email: EMAIL, password: PASSWORD } })
    this.accessToken = data.accessToken
  }

  async refresh() {
    const response = await this.raw('POST', '/api/auth/refresh')
    if (!response.ok) {
      throw new Error(`Session expired and refresh failed (${response.status})`)
    }
    const data = await response.json()
    this.accessToken = data.accessToken
  }
}

function safeParse(text) {
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

async function loadExistingProducts(client) {
  const byRef = new Map()
  for (let page = 0; ; page++) {
    const result = await client.request('GET', `/api/admin/products?page=${page}&size=50`)
    for (const product of result.content) {
      byRef.set(product.ref, product.id)
    }
    if (result.last || result.content.length === 0) {
      return byRef
    }
  }
}

async function seedProduct(client, product, existing, counters) {
  let productId = existing.get(product.ref)
  if (!productId) {
    const created = await client.request('POST', '/api/admin/products', {
      json: {
        ref: product.ref,
        name: product.name,
        description: product.description,
        price: product.price,
        discountPercentage: product.discountPercentage,
        category: product.category,
        featuredSlot: product.featuredSlot
      }
    })
    productId = created.id
    existing.set(product.ref, productId)
    counters.products++
  }

  const skus = await client.request('GET', `/api/admin/products/${productId}/skus`)
  const bySku = new Map(skus.map((sku) => [`${sku.color}:${sku.size}`, sku]))

  for (const { color, size } of expandSkus(product)) {
    let sku = bySku.get(`${color}:${size}`)
    if (!sku) {
      sku = await client.request('POST', `/api/admin/products/${productId}/skus`, {
        json: {
          size,
          color,
          weightGrams: product.weightGrams,
          lengthCm: product.lengthCm,
          widthCm: product.widthCm,
          heightCm: product.heightCm,
          discountPercentage: 0
        }
      })
      counters.skus++
    }

    if (!SKIP_IMAGES && !sku.cover) {
      const png = imageFor(product.shape, color)
      await client.request('POST', `/api/admin/products/${productId}/skus/${sku.id}/images`, {
        form: () => {
          const data = new FormData()
          data.append('file', new Blob([png], { type: 'image/png' }), `${product.ref.toLowerCase()}-${color.toLowerCase()}.png`)
          return data
        }
      })
      counters.images++
    }

    if (sku.stock === 0) {
      await client.request('PUT', `/api/admin/stock/${sku.id}`, { json: { stock: STOCK } })
      counters.stock++
    }
  }
}

async function main() {
  summary()

  if (PREVIEW) {
    preview()
    return
  }
  if (DRY_RUN) {
    for (const product of selected) {
      console.log(`${product.ref}  ${product.name}  ${product.price} EUR  -${product.discountPercentage}%  ${product.category}`)
    }
    return
  }
  if (!API_URL || !EMAIL || !PASSWORD) {
    console.error('Set SEED_API_URL, SEED_ADMIN_EMAIL and SEED_ADMIN_PASSWORD first.')
    process.exit(1)
  }

  const client = new ApiClient(API_URL)
  console.log(`Logging in to ${API_URL} (the first call can take ~20 s if the server is asleep)...`)
  await client.login()
  const existing = await loadExistingProducts(client)
  console.log(`${existing.size} products already exist`)

  const counters = { products: 0, skus: 0, images: 0, stock: 0 }
  const failures = []
  for (const [index, product] of selected.entries()) {
    try {
      await seedProduct(client, product, existing, counters)
      console.log(`[${index + 1}/${selected.length}] ${product.ref} ${product.name}`)
    } catch (error) {
      failures.push(`${product.ref}: ${error.message}`)
      console.error(`[${index + 1}/${selected.length}] ${product.ref} FAILED: ${error.message}`)
    }
  }

  console.log(`Created ${counters.products} products, ${counters.skus} variants, ${counters.images} images, stock on ${counters.stock}.`)
  if (failures.length > 0) {
    console.error(`${failures.length} failed. Run the script again, it continues where it stopped.`)
    process.exit(1)
  }
}

main().catch((error) => {
  console.error(error.message)
  process.exit(1)
})
