import type { ProductColor, ProductSize } from '@/types/product'

export interface AdminCreateProductSkuRequest extends AdminUpdateProductSkuRequest {
  size: ProductSize
  color: ProductColor
}

export interface AdminUpdateProductSkuRequest {
  weight: number
  length: number
  width: number
  height: number
  discountPercentage: number
}

export interface AdminProductSkuResponse {
  id: number
  size: ProductSize
  color: ProductColor
  cover?: string
  stock: number
  weight: number
  length: number
  width: number
  height: number
  discountPercentage: number
  active: boolean
}
