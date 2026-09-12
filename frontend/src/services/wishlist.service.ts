import { api } from '@/lib/api'
import type { ProductResponse } from '@/types/product'

export const wishlistService = {
  addToWishlist: async (productId: number) => {
    await api.post<void>(`/api/wishlist/${productId}`)
  },

  mergeWishlist: async (productIds: number[]) => {
    await api.post<void>('/api/wishlist/merge', { productIds })
  },

  getWishlist: async () => {
    const response = await api.get<ProductResponse[]>('/api/wishlist')

    return response.data
  },

  removeFromWishlist: async (productId: number) => {
    await api.delete<void>(`/api/wishlist/${productId}`)
  },

  clearWishlist: async () => {
    await api.delete<void>('/api/wishlist')
  }
}
