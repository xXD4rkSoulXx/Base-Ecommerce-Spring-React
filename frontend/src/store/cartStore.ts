import { create } from 'zustand'
import { persist } from 'zustand/middleware'

import type { Product } from '@/types/Product'

interface LocalCartItem {
  product: Product;
  quantity: number;
}

interface CartState {
  items: LocalCartItem[];
  addToCart: (product: Product) => void;
  removeFromCart: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      addToCart: (product) => {
        if (get().items.find((item) => item.product.id === product.id)) {
          set({
            items: get().items.map((item) =>
              item.product.id === product.id
                ? { ...item, quantity: item.quantity + 1 }
                : item
            )
          });
        } else {
          set({ items: [...get().items, { product, quantity: 1 }] });
        }
      },
      removeFromCart: (productId) => {
        set({items: get().items.filter((item) => item.product.id !== productId)});
      },
      updateQuantity: (productId, quantity) => {
        if (quantity <= 0) {
          get().removeFromCart(productId);
          return;
        }
        set({
          items: get().items.map((item) =>
            item.product.id === productId
              ? { ...item, quantity }
              : item
          )
        });
      },
      clearCart: () => set({ items: [] })
    }),
    {
      name: 'velo-citi',
    }
  )
);
