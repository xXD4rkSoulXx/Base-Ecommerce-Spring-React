import type { ProductColor, ProductSize } from '@/types/product'
import type { Schema } from '@/types/schema'

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

export type AdminProductSkuResponse = Schema<'AdminProductSkuResponse'>
