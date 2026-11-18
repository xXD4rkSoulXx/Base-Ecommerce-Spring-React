import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { ProductResponse } from '@/types/product'

interface WishlistState {
  items: ProductResponse[]
  addToWishlist: (product: ProductResponse) => void
  getTotalItems: () => number
  removeFromWishlist: (productId: number) => void
  clearWishlist: () => void
}

export const MAX_GUEST_WISHLIST_ITEMS = 100

export const useWishlistStore = create<WishlistState>()(
  persist(
    (set, get) => ({
      items: [],

      addToWishlist: (product) =>
        set((state) => {
          const itemExists = state.items.some((item) => item.id === product.id)
          if (itemExists || state.items.length >= MAX_GUEST_WISHLIST_ITEMS) {
            return state
          }

          return { items: [...state.items, product] }
        }),

      getTotalItems: () => get().items.length,

      removeFromWishlist: (productId) =>
        set((state) => ({
          items: state.items.filter((item) => item.id !== productId)
        })),

      clearWishlist: () => set({ items: [] })
    }),
    {
      name: 'velociti-wishlist'
    }
  )
)
