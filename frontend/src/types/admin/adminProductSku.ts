import type { ProductColor, ProductSize } from '@/types/product'

export interface AdminCreateProductSkuRequest extends AdminUpdateProductSkuRequest {
  size: ProductSize
  color: ProductColor
}

export interface AdminUpdateProductSkuRequest {
  weightGrams: number
  lengthCm: number
  widthCm: number
  heightCm: number
  discountPercentage: number
}

export interface AdminProductSkuResponse {
  id: number
  size: ProductSize
  color: ProductColor
  cover?: string
  stock: number
  weightGrams: number
  lengthCm: number
  widthCm: number
  heightCm: number
  discountPercentage: number
  active: boolean
}
