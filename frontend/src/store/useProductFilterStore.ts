import { create } from 'zustand'

import type { ProductCategory } from '@/types/Product';

interface FilterState {
  category: ProductCategory | "ALL";
  sortBy: "recent" | "price_asc" | "price_desc";
  setCategory: (category: ProductCategory | "ALL") => void;
  setSortBy: (sortBy: "recent" | "price_asc" | "price_desc") => void;
  resetFilters: () => void;
}

export const useProductFilterStore = create<FilterState>((set) => ({
  category: "ALL",
  sortBy: "recent",
  
  setCategory: (category) => set({ category }),

  setSortBy: (sortBy) => set({ sortBy }),

  resetFilters: () => set({ category: "ALL", sortBy: "recent" })
}));
