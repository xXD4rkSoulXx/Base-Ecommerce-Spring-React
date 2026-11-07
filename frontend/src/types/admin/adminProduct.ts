import type { FeaturedSlot, ProductCategory } from '@/types/product'

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

export interface AdminProductSummaryResponse {
  id: number
  ref: string
  name: string
  category: ProductCategory
  price: number
  discountPercentage: number
  featuredSlot?: FeaturedSlot
  active: boolean
  cover?: string
  totalStock: number
  skuCount: number
  lowStockSkus: number
}

export interface AdminProductDetailsResponse {
  id: number
  ref: string
  name: string
  description: string
  price: number
  discountPercentage: number
  category: ProductCategory
  featuredSlot?: FeaturedSlot
  active: boolean
}
