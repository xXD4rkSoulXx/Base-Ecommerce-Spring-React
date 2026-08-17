import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { ProductResponse } from '@/types/product'

interface LocalCartItem {
  product: ProductResponse
  quantity: number
}

interface CartState {
  items: LocalCartItem[]
  addToCart: (product: ProductResponse) => void
  removeFromCart: (productId: number) => void
  updateQuantity: (productId: number, quantity: number) => void
  clearCart: () => void
  getTotalItems: () => number
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],

      addToCart: (product) => set((state) => {
        const itemExists = state.items.some((item) => item.product.id === product.id)
        const newItems = itemExists
          ? state.items.map((item) =>
              item.product.id === product.id
                ? { ...item, quantity: item.quantity + 1 }
                : item
            )
          : [...state.items, { product, quantity: 1 }]

        return { items: newItems }
      }),

      removeFromCart: (productId) => set((state) => ({
        items: state.items.filter((item) => item.product.id !== productId)
      })),

      updateQuantity: (productId, quantity) => set((state) => {
        if (quantity <= 0) {
          return { items: state.items.filter((item) => item.product.id !== productId) }
        }
        
        return {
          items: state.items.map((item) =>
            item.product.id === productId
              ? { ...item, quantity }
              : item
          )
        }
      }),
      
      clearCart: () => set({ items: [] }),

      getTotalItems: () => {
        return get().items.reduce((total, item) => total + item.quantity, 0)
      }
    }),
    {
      name: 'velociti-cart'
    }
  )
)

useCartStore.persist.rehydrate()
