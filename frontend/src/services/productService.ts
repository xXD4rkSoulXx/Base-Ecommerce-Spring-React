import type { ProductCategory, ProductResponse, PageResponse } from '@/types/Product'

import { api } from '@/services/api'

interface ProductService {
  fetchShopProducts: (
    category: ProductCategory | 'ALL' | null,
    sortBy: 'recent' | 'price_asc' | 'price_desc' | string | null,
    pageParam: number
  ) => Promise<PageResponse<ProductResponse>>;
}

export const productService: ProductService = {
  fetchShopProducts: async (category, sortBy, pageParam) => {
    const categoryParam = category === 'ALL' || !category ? undefined : category; 
     
    const response = await api.get<PageResponse<ProductResponse>>('/api/products', {
      params: {
        category: categoryParam,
        sortBy: sortBy || 'recent',
        page: pageParam,
        size: 12
      }
    });
    
    return response.data;
  }
};
