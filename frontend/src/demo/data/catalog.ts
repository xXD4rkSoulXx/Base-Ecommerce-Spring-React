import type { ProductCategory, ProductColor, ProductSize, FeaturedSlot } from '@/types/product'
import type { DemoProduct, DemoSku } from '@/demo/types'

const COLOR_HEX: Record<ProductColor, string> = {
  BLACK: '#18181b',
  BLUE: '#2563eb',
  CYAN: '#06b6d4',
  GOLD: '#ca8a04',
  GRAY: '#71717a',
  GREEN: '#16a34a',
  LIME: '#84cc16',
  MAGENTA: '#c026d3',
  MAROON: '#7f1d1d',
  OLIVE: '#656d20',
  ORANGE: '#ea580c',
  PINK: '#ec4899',
  PURPLE: '#7c3aed',
  RED: '#dc2626',
  SALMON: '#f87171',
  TEAL: '#0d9488',
  WHITE: '#e4e4e7',
  YELLOW: '#eab308'
}

const DIMENSIONS: Record<ProductCategory, Pick<DemoSku, 'weightGrams' | 'lengthCm' | 'widthCm' | 'heightCm'>> = {
  OUTERWEAR: { weightGrams: 900, lengthCm: 40, widthCm: 30, heightCm: 12 },
  TOPS: { weightGrams: 300, lengthCm: 30, widthCm: 25, heightCm: 4 },
  BOTTOMS: { weightGrams: 500, lengthCm: 35, widthCm: 28, heightCm: 6 },
  FOOTWEAR: { weightGrams: 1100, lengthCm: 34, widthCm: 22, heightCm: 13 },
  UNDERWEAR: { weightGrams: 150, lengthCm: 22, widthCm: 16, heightCm: 3 },
  ACCESSORIES: { weightGrams: 250, lengthCm: 25, widthCm: 20, heightCm: 8 }
}

const artwork = (label: string, color: ProductColor, variant: number) => {
  const fill = COLOR_HEX[color]
  const shade = variant === 0 ? '#0a0a0f' : variant === 1 ? '#15151f' : '#1f1f2e'
  const svg =
    `<svg xmlns="http://www.w3.org/2000/svg" width="480" height="600" viewBox="0 0 480 600">` +
    `<rect width="480" height="600" fill="${shade}"/>` +
    `<rect x="60" y="80" width="360" height="440" fill="none" stroke="${fill}" stroke-width="3"/>` +
    `<circle cx="240" cy="260" r="${70 + variant * 18}" fill="${fill}" opacity="0.85"/>` +
    `<text x="240" y="440" font-family="monospace" font-size="26" fill="#e4e4e7" text-anchor="middle">${label}</text>` +
    `<text x="240" y="480" font-family="monospace" font-size="16" fill="${fill}" text-anchor="middle">DEMO</text>` +
    `</svg>`

  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`
}

interface ProductSeed {
  ref: string
  name: string
  description: string
  category: ProductCategory
  price: number
  discountPercentage?: number
  featuredSlot?: FeaturedSlot
  active?: boolean
  skus: SkuSeed[]
}

interface SkuSeed {
  size: ProductSize
  color: ProductColor
  stock: number
  discountPercentage?: number
  active?: boolean
  images?: number
}

const sku = (size: ProductSize, color: ProductColor, stock: number, extra: Partial<SkuSeed> = {}): SkuSeed => ({
  size,
  color,
  stock,
  ...extra
})

const PRODUCT_SEEDS: ProductSeed[] = [
  {
    ref: 'VC-OUT-001',
    name: 'Neon Drift Jacket',
    description: 'Lightweight windbreaker with reflective piping and a packable hood.',
    category: 'OUTERWEAR',
    price: 149.9,
    featuredSlot: 'LEFT',
    skus: [
      sku('S', 'BLACK', 14, { images: 3 }),
      sku('M', 'BLACK', 22),
      sku('L', 'BLACK', 3),
      sku('M', 'CYAN', 9),
      sku('L', 'CYAN', 0)
    ]
  },
  {
    ref: 'VC-OUT-002',
    name: 'Grid Runner Parka',
    description: 'Insulated parka built for wet winters, with taped seams and a storm flap.',
    category: 'OUTERWEAR',
    price: 219,
    discountPercentage: 15,
    skus: [sku('M', 'OLIVE', 8), sku('L', 'OLIVE', 11), sku('XL', 'OLIVE', 5), sku('M', 'BLACK', 6)]
  },
  {
    ref: 'VC-OUT-003',
    name: 'Static Bomber',
    description: 'Retired capsule bomber. Archived and no longer sold.',
    category: 'OUTERWEAR',
    price: 129,
    active: false,
    skus: [sku('M', 'MAROON', 2), sku('L', 'MAROON', 0)]
  },
  {
    ref: 'VC-TOP-001',
    name: 'Signal Tee',
    description: 'Heavyweight cotton tee with a boxy cut and a tonal chest print.',
    category: 'TOPS',
    price: 39.9,
    skus: [
      sku('S', 'WHITE', 40),
      sku('M', 'WHITE', 52),
      sku('L', 'WHITE', 35),
      sku('M', 'BLACK', 61),
      sku('L', 'BLACK', 4),
      sku('XL', 'BLACK', 18),
      sku('M', 'LIME', 12, { discountPercentage: 25 })
    ]
  },
  {
    ref: 'VC-TOP-002',
    name: 'Circuit Hoodie',
    description: 'Brushed fleece hoodie with a woven circuit label and a kangaroo pocket.',
    category: 'TOPS',
    price: 89,
    discountPercentage: 10,
    featuredSlot: 'RIGHT',
    skus: [
      sku('S', 'GRAY', 17),
      sku('M', 'GRAY', 25),
      sku('L', 'GRAY', 19),
      sku('M', 'PURPLE', 7),
      sku('L', 'PURPLE', 1)
    ]
  },
  {
    ref: 'VC-TOP-003',
    name: 'Pulse Longsleeve',
    description: 'Seamless performance longsleeve with a ribbed collar.',
    category: 'TOPS',
    price: 49.9,
    skus: [sku('S', 'TEAL', 13), sku('M', 'TEAL', 16), sku('L', 'TEAL', 9, { active: false })]
  },
  {
    ref: 'VC-BOT-001',
    name: 'Cargo Relay Pants',
    description: 'Tapered cargo pants with six pockets and adjustable cuffs.',
    category: 'BOTTOMS',
    price: 99,
    skus: [
      sku('S', 'OLIVE', 10),
      sku('M', 'OLIVE', 21),
      sku('L', 'OLIVE', 14),
      sku('M', 'BLACK', 28),
      sku('L', 'BLACK', 2)
    ]
  },
  {
    ref: 'VC-BOT-002',
    name: 'Night Shift Joggers',
    description: 'Soft-touch joggers with zip ankles, ideal for late shifts.',
    category: 'BOTTOMS',
    price: 69.9,
    discountPercentage: 20,
    skus: [sku('S', 'BLACK', 30), sku('M', 'BLACK', 44), sku('L', 'BLACK', 27), sku('XL', 'BLACK', 12)]
  },
  {
    ref: 'VC-BOT-003',
    name: 'Transit Shorts',
    description: 'Quick-dry shorts with a hidden zip pocket.',
    category: 'BOTTOMS',
    price: 45,
    skus: [sku('M', 'BLUE', 0), sku('L', 'BLUE', 0), sku('M', 'ORANGE', 6)]
  },
  {
    ref: 'VC-FTW-001',
    name: 'Aero Pulse Sneakers',
    description: 'Responsive running sneakers with a breathable engineered mesh.',
    category: 'FOOTWEAR',
    price: 139,
    skus: [
      sku('_40', 'WHITE', 6),
      sku('_41', 'WHITE', 12),
      sku('_42', 'WHITE', 15),
      sku('_43', 'WHITE', 9),
      sku('_44', 'WHITE', 3),
      sku('_42', 'MAGENTA', 8)
    ]
  },
  {
    ref: 'VC-FTW-002',
    name: 'Vector High-Tops',
    description: 'Vulcanised high-tops with a padded collar and reinforced toe.',
    category: 'FOOTWEAR',
    price: 159,
    discountPercentage: 10,
    skus: [sku('_41', 'BLACK', 11), sku('_42', 'BLACK', 14), sku('_43', 'BLACK', 10), sku('_44', 'BLACK', 5)]
  },
  {
    ref: 'VC-FTW-003',
    name: 'Trail Phantom Boots',
    description: 'Waterproof trail boots with a grippy lugged outsole.',
    category: 'FOOTWEAR',
    price: 189,
    skus: [sku('_42', 'OLIVE', 0), sku('_43', 'OLIVE', 0), sku('_44', 'OLIVE', 0)]
  },
  {
    ref: 'VC-UND-001',
    name: 'Base Layer Pack',
    description: 'Pack of three seamless base layer tops.',
    category: 'UNDERWEAR',
    price: 29.9,
    skus: [sku('S', 'BLACK', 70), sku('M', 'BLACK', 85), sku('L', 'BLACK', 64), sku('M', 'WHITE', 48)]
  },
  {
    ref: 'VC-UND-002',
    name: 'Thermal Brief Set',
    description: 'Two-pack of thermal briefs with a soft elastic waistband.',
    category: 'UNDERWEAR',
    price: 34.9,
    skus: [sku('M', 'GRAY', 33), sku('L', 'GRAY', 29), sku('XL', 'GRAY', 5)]
  },
  {
    ref: 'VC-ACC-001',
    name: 'Cipher Beanie',
    description: 'Merino blend beanie with a woven cipher patch.',
    category: 'ACCESSORIES',
    price: 24.9,
    skus: [sku('ONE_SIZE', 'BLACK', 58), sku('ONE_SIZE', 'YELLOW', 22), sku('ONE_SIZE', 'RED', 4)]
  },
  {
    ref: 'VC-ACC-002',
    name: 'Loop Crossbody Bag',
    description: 'Compact water-resistant crossbody bag with a magnetic buckle.',
    category: 'ACCESSORIES',
    price: 59,
    skus: [sku('ONE_SIZE', 'BLACK', 26), sku('ONE_SIZE', 'GOLD', 10), sku('ONE_SIZE', 'SALMON', 0)]
  }
]

export const sizeLabel = (size: ProductSize) => (size === 'ONE_SIZE' ? 'One size' : size.replace('_', ''))

export const buildCatalog = (): DemoProduct[] => {
  let nextSkuId = 1
  let nextImageId = 1

  return PRODUCT_SEEDS.map((seed, index) => {
    const productId = index + 1
    const dimensions = DIMENSIONS[seed.category]
    const productCoverSkuIndex = seed.skus.findIndex((skuSeed) => (skuSeed.active ?? true) && (skuSeed.images ?? 2) > 0)

    const skus: DemoSku[] = seed.skus.map((skuSeed, skuIndex) => {
      const imageCount = skuSeed.images ?? 2
      const label = `${seed.ref} ${sizeLabel(skuSeed.size)}`

      return {
        id: nextSkuId++,
        productId,
        size: skuSeed.size,
        color: skuSeed.color,
        stock: skuSeed.stock,
        ...dimensions,
        discountPercentage: skuSeed.discountPercentage ?? 0,
        active: skuSeed.active ?? true,
        images: Array.from({ length: imageCount }, (_, imageIndex) => ({
          id: nextImageId++,
          path: artwork(label, skuSeed.color, imageIndex),
          skuCover: imageIndex === 0,
          productCover: skuIndex === productCoverSkuIndex && imageIndex === 0
        }))
      }
    })

    return {
      id: productId,
      ref: seed.ref,
      name: seed.name,
      description: seed.description,
      price: seed.price,
      discountPercentage: seed.discountPercentage ?? 0,
      category: seed.category,
      featuredSlot: seed.featuredSlot,
      active: seed.active ?? true,
      skus
    }
  })
}

export const getSkuCover = (sku: DemoSku) => sku.images.find((image) => image.skuCover)?.path

export const getProductCover = (product: DemoProduct) =>
  product.skus.flatMap((sku) => sku.images).find((image) => image.productCover)?.path
