import type { ProductCategory, ProductResponse, SpringPage } from '@/types/Product'

import { api } from '@/services/api'

interface ProductService {
  fetchShopProducts: (
    category: ProductCategory | 'ALL' | null,
    sortBy: 'recent' | 'price_asc' | 'price_desc' | string | null,
    pageParam: number
  ) => Promise<SpringPage<ProductResponse>>;
}

export const productService: ProductService = {
  fetchShopProducts: async (category, sortBy, pageParam) => {
    const categoryParam = category === 'ALL' || !category ? undefined : category; 
     
    const response = await api.get<SpringPage<ProductResponse>>('/api/products', {
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
