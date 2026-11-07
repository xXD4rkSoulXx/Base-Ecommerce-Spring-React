import type { ProductColor, ProductSize } from '@/types/product'

export interface AdminStockSearchRequest {
  q?: string
  lowStock?: boolean
  page?: number
  size?: number
}

export interface AdminAdjustStockRequest {
  delta: number
}

export interface AdminSetStockRequest {
  stock: number
}

export interface AdminStockItemResponse {
  skuId: number
  productId: number
  productName: string
  productRef: string
  size: ProductSize
  color: ProductColor
  cover?: string
  stock: number
  lowStock: boolean
}
