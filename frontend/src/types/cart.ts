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

export interface CartResponse {
  items: CartItemResponse[]
  discountCode: string | null
  discountPercentage: number
}

export interface CartItemResponse {
  id: number
  productSkuId: number
  productId: number
  productName: string
  productRef: string
  size: string
  color: string
  cover: string
  price: number
  discountPercentage: number
  stock: number
  quantity: number
}
