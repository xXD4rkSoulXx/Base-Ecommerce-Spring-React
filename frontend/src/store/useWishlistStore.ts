import { create } from 'zustand'
import { persist } from 'zustand/middleware'

import type { Product } from '@/types/Product'

interface WishlistState {
  items: Product[];
  addtoWishlist: (product: Product) => void;
  removeFromWishlist: (productId: number) => void;
  clearWishlist: () => void;
  isInWishlist: (productId: number) => boolean;
  getTotalItems: () => number;
}

export const useWishlistStore = create<WishlistState>()(
  persist(
    (set, get) => ({
      items: [],

      addtoWishlist: (product) => set((state) => {
        if (state.items.some((item) => item.id === product.id)) {
            return state;
        }
        
        return { items: [...state.items, product] };
      }),

      removeFromWishlist: (productId) => set((state) => ({
        items: state.items.filter((item) => item.id !== productId)
      })),

      clearWishlist: () => set({ items: [] }),

      isInWishlist: (productId) => {
        return get().items.some((item) => item.id === productId);
      },

       getTotalItems: () => {
        return get().items.length;
      }
    }),
    {
      name: "velociti-wishlist"
    }
  )
);
