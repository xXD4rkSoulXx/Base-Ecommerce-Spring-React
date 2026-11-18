import { api } from '@/lib/api'
import type {
  AddToCartRequest,
  ApplyDiscountCodeRequest,
  CartResponse,
  MergeCartRequest,
  UpdateCartItemRequest
} from '@/types/cart'

export const cartService = {
  addToCart: async (request: AddToCartRequest) => {
    const response = await api.post<CartResponse>('/api/cart', request)

    return response.data
  },

  mergeCart: async (request: MergeCartRequest) => {
    await api.post<void>('/api/cart/merge', request)
  },

  applyDiscountCode: async (request: ApplyDiscountCodeRequest) => {
    const response = await api.post<CartResponse>('/api/cart/discount', request)

    return response.data
  },

  getCart: async () => {
    const response = await api.get<CartResponse>('/api/cart')

    return response.data
  },

  updateQuantity: async (productSkuId: number, request: UpdateCartItemRequest) => {
    const response = await api.put<CartResponse>(`/api/cart/${productSkuId}`, request)

    return response.data
  },

  removeFromCart: async (productSkuId: number) => {
    const response = await api.delete<CartResponse>(`/api/cart/${productSkuId}`)

    return response.data
  },

  removeDiscountCode: async () => {
    const response = await api.delete<CartResponse>('/api/cart/discount')

    return response.data
  },

  clearCart: async () => {
    await api.delete<void>('/api/cart')
  }
}
