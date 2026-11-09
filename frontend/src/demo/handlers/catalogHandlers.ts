import { http, HttpResponse, type HttpHandler } from 'msw'
import type { AdminProductDetailsResponse, AdminProductSummaryResponse } from '@/types/admin/adminProduct'
import type { AdminProductSkuResponse } from '@/types/admin/adminProductSku'
import type { AdminStockItemResponse } from '@/types/admin/adminStock'
import type { ProductCategory } from '@/types/product'
import type { DemoProduct, DemoSku } from '@/demo/types'
import { getProductCover, getSkuCover } from '@/demo/data/catalog'
import { getFixtures } from '@/demo/data/fixtures'
import { matchesQuery, notFound, paginate, readBoolean, readId } from '@/demo/handlers/support'

export const LOW_STOCK_THRESHOLD = 5

const findProduct = (id: number) => getFixtures().products.find((product) => product.id === id)

const toSkuResponse = (sku: DemoSku): AdminProductSkuResponse => ({
  id: sku.id,
  size: sku.size,
  color: sku.color,
  cover: getSkuCover(sku),
  stock: sku.stock,
  weight: sku.weight,
  length: sku.length,
  width: sku.width,
  height: sku.height,
  discountPercentage: sku.discountPercentage,
  active: sku.active
})

const toDetails = (product: DemoProduct): AdminProductDetailsResponse => ({
  id: product.id,
  ref: product.ref,
  name: product.name,
  description: product.description,
  price: product.price,
  discountPercentage: product.discountPercentage,
  category: product.category,
  featuredSlot: product.featuredSlot,
  active: product.active
})

const toSummary = (product: DemoProduct): AdminProductSummaryResponse => {
  const activeSkus = product.skus.filter((sku) => sku.active)

  return {
    ...toDetails(product),
    cover: getProductCover(product),
    totalStock: activeSkus.reduce((sum, sku) => sum + sku.stock, 0),
    skuCount: activeSkus.length,
    lowStockSkus: activeSkus.filter((sku) => sku.stock <= LOW_STOCK_THRESHOLD).length
  }
}

export const catalogHandlers: HttpHandler[] = [
  http.get('*/api/admin/products', ({ request }) => {
    const url = new URL(request.url)
    const q = url.searchParams.get('q')
    const category = url.searchParams.get('category') as ProductCategory | null
    const lowStock = readBoolean(url, 'lowStock')

    const products = getFixtures()
      .products.map(toSummary)
      .filter((product) => matchesQuery(q, product.name, product.ref))
      .filter((product) => !category || product.category === category)
      .filter((product) => !lowStock || product.lowStockSkus > 0)
      .sort((a, b) => b.id - a.id)

    return HttpResponse.json(paginate(products, url))
  }),

  http.get('*/api/admin/products/:id', ({ params }) => {
    const product = findProduct(readId(params.id))

    return product ? HttpResponse.json(toDetails(product)) : notFound()
  }),

  http.get('*/api/admin/products/:id/skus', ({ params }) => {
    const product = findProduct(readId(params.id))

    return product ? HttpResponse.json(product.skus.map(toSkuResponse)) : notFound()
  }),

  http.get('*/api/admin/products/:productId/skus/:skuId/images', ({ params }) => {
    const sku = findProduct(readId(params.productId))?.skus.find((candidate) => candidate.id === readId(params.skuId))

    return sku ? HttpResponse.json(sku.images) : notFound()
  }),

  http.get('*/api/admin/stock', ({ request }) => {
    const url = new URL(request.url)
    const q = url.searchParams.get('q')
    const lowStock = readBoolean(url, 'lowStock')

    const items: AdminStockItemResponse[] = getFixtures()
      .products.filter((product) => product.active)
      .flatMap((product) =>
        product.skus
          .filter((sku) => sku.active)
          .map((sku) => ({
            skuId: sku.id,
            productId: product.id,
            productName: product.name,
            productRef: product.ref,
            size: sku.size,
            color: sku.color,
            cover: getSkuCover(sku),
            stock: sku.stock,
            lowStock: sku.stock <= LOW_STOCK_THRESHOLD
          }))
      )
      .filter((item) => matchesQuery(q, item.productName, item.productRef, item.color, item.size))
      .filter((item) => !lowStock || item.lowStock)
      .sort((a, b) => a.stock - b.stock || a.productName.localeCompare(b.productName))

    return HttpResponse.json(paginate(items, url))
  })
]
