import type { FeaturedSlot, ProductCategory } from '@/types/product'
import type { Schema } from '@/types/schema'

export interface AdminProductRequest {
  ref: string
  name: string
  description: string
  price: number
  discountPercentage: number
  category: ProductCategory
  featuredSlot?: FeaturedSlot
}

export interface AdminProductSearchRequest {
  q?: string
  category?: ProductCategory
  lowStock?: boolean
  page?: number
  size?: number
}

export type AdminProductSummaryResponse = Schema<'AdminProductSummaryResponse'>

export type AdminProductDetailsResponse = Schema<'AdminProductDetailsResponse'>
