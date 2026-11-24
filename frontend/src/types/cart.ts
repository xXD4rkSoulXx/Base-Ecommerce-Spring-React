import type { Schema } from '@/types/schema'

export interface AddToCartRequest {
  productSkuId: number
  quantity: number
}

export interface MergeCartRequest {
  items: MergeCartItemRequest[]
}

export interface MergeCartItemRequest {
  productSkuId: number
  quantity: number
}

export interface ApplyDiscountCodeRequest {
  code: string
}

export interface UpdateCartItemRequest {
  quantity: number
}

export type CartResponse = Schema<'CartResponse'>

export type CartItemResponse = Schema<'CartItemResponse'>
