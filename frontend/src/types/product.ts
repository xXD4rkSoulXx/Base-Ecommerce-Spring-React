import type { Schema } from '@/types/schema'

export type FeaturedSlot = NonNullable<Schema<'ProductResponse'>['featuredSlot']>

export type ProductCategory = Schema<'ProductResponse'>['category']

export type ProductSize = Schema<'ProductSkuDetailsResponse'>['size']

export type ProductColor = Schema<'ProductSkuDetailsResponse'>['color']

export type ProductSort = 'recent' | 'price_asc' | 'price_desc' | 'best_sellers' | 'top_rated' | 'discount'

export interface ProductShopRequest {
  category?: ProductCategory | 'ALL'
  page?: number
  size?: number
  sortBy?: ProductSort
}

export interface ProductSearchRequest {
  q?: string
  page?: number
  size?: number
}

export interface ProductReviewsRequest {
  page?: number
  size?: number
}

export type ProductResponse = Schema<'ProductResponse'>

export type ProductDetailsResponse = Schema<'ProductDetailsResponse'>

export type ProductSkuDetailsResponse = Schema<'ProductSkuDetailsResponse'>
