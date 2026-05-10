import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { Product } from "@/types/Product";

export type CartItem = {
  id: string;
  product: Product;
  size: string;
  qty: number;
};

type CartState = {
  items: CartItem[];
  addItem: (product: Product, size: string, qty?: number) => void;
  removeItem: (key: string) => void;
  updateQty: (key: string, qty: number) => void;
  clear: () => void;
  total: () => number;
  count: () => number;
};

export const useCart = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      addItem: (product, size, qty = 1) => {
        const key = `${product.id}-${size}`;
        const existing = get().items.find((i) => i.id === key);
        if (existing) {
          set({
            items: get().items.map((i) =>
              i.id === key ? { ...i, qty: i.qty + qty } : i
            ),
          });
        } else {
          set({ items: [...get().items, { id: key, product, size, qty }] });
        }
      },
      removeItem: (key) =>
        set({ items: get().items.filter((i) => i.id !== key) }),
      updateQty: (key, qty) =>
        set({
          items:
            qty <= 0
              ? get().items.filter((i) => i.id !== key)
              : get().items.map((i) => (i.id === key ? { ...i, qty } : i)),
        }),
      clear: () => set({ items: [] }),
      total: () =>
        get().items.reduce((sum, i) => sum + i.product.price * i.qty, 0),
      count: () => get().items.reduce((sum, i) => sum + i.qty, 0),
    }),
    { name: "velociti-cart" }
  )
);
