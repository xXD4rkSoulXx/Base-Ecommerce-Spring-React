import type { Schema } from '@/types/schema'

export interface AdminCreateDiscountCodeRequest extends AdminUpdateDiscountCodeRequest {
  code: string
}

export interface AdminUpdateDiscountCodeRequest {
  discountPercentage: number
}

export interface AdminDiscountCodeSearchRequest {
  q?: string
  active?: boolean
  page: number
  size: number
}

export type AdminDiscountCodeResponse = Schema<'AdminDiscountCodeResponse'>
