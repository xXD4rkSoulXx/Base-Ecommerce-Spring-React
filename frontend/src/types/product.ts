export type ProductCategory = 'OUTERWEAR' | 'TOPS' | 'BOTTOMS' | 'FOOTWEAR' | 'UNDERWEAR'
  | 'ACCESSORIES'
export type ProductSize = 'XS' | 'S' | 'M' | 'L' | 'XL' | 'XXL'
  | '_15' | '_16' | '_17' | '_18' | '_19' | '_20' | '_21' | '_22' | '_23' | '_24' | '_25'
  | '_26' | '_27' | '_28' | '_29' | '_30' | '_31' | '_32' | '_33' | '_34' | '_35' | '_36'
  | '_37' | '_38' | '_39' | '_40' | '_41' | '_42' | '_43' | '_44' | '_45' | '_46'
export type ProductColor = 'BLACK' | 'BLUE' | 'CYAN' | 'GOLD' | 'GRAY' | 'GREEN' | 'LIME'
  | 'MAGENTA' | 'MARRON' | 'OLIVE' | 'ORANGE' | 'PINK' | 'PURPLE' | 'RED' | 'SALMON'
  | 'TEAL' | 'WHITE' | 'YELLOW'

export interface ProductShopRequest {
  category?: ProductCategory | 'ALL'
  page?: number
  size?: number
  sortBy?: 'recent' | 'price_asc' | 'price_desc' | (string & {})
}

export interface ProductSearchRequest {
  q?: string
  page?: number
  size?: number
}

export interface ProductResponse {
  id: number
  ref: string
  name: string
  price: number
  discountPercentage: number
  category: ProductCategory
  cover: string
}

export interface ProductDetailsResponse {
  id: number
  ref: string
  name: string
  description: string
  price: number
  discountPercentage: number
  category: ProductCategory
  averageRating: number
  totalReviews: number
  productSkus: ProductSkuDetailsResponse[]
}

export interface ProductSkuDetailsResponse {
  id: number
  size: ProductSize
  color: ProductColor
  stock: number
  discountPercentage: number
  paths: string[]
}

export interface ProductReviewsRequest {
  page?: number
  size?: number
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
