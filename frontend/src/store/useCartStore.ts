import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { CartItemResponse } from '@/types/cart'

export type GuestCartItem = Omit<CartItemResponse, 'id'>

interface CartState {
  items: GuestCartItem[]
  addToCart: (item: GuestCartItem) => void
  getTotalItems: () => number
  updateQuantity: (productSkuId: number, quantity: number) => void
  removeFromCart: (productSkuId: number) => void
  clearCart: () => void
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],

      addToCart: (item) => set((state) => {
        const existingItem = state.items.find((i) => i.productSkuId === item.productSkuId)

        if (existingItem) {
          return {
            items: state.items.map((i) =>
              i.productSkuId === item.productSkuId
                ? { ...i, quantity: i.quantity + item.quantity }
                : i
            )
          }
        }

        return { items: [...state.items, item] }
      }),

      getTotalItems: () => {
        return get().items.reduce((total, item) => total + item.quantity, 0)
      },

      updateQuantity: (productSkuId, quantity) => set((state) => {
        if (quantity <= 0) {
          return { items: state.items.filter((item) => item.productSkuId !== productSkuId) }
        }

        return {
          items: state.items.map((item) =>
            item.productSkuId === productSkuId
              ? { ...item, quantity }
              : item
          )
        }
      }),

      removeFromCart: (productSkuId) => set((state) => ({
        items: state.items.filter((item) => item.productSkuId !== productSkuId)
      })),

      clearCart: () => set({ items: [] })
    }),
    {
      name: 'velociti-cart'
    }
  )
)
