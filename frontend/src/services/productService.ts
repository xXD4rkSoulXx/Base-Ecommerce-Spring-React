import type { ProductShopRequest, ProductSearchRequest, ProductResponse, PageResponse } from '@/types/Product'

import { api } from '@/services/api'

interface ProductService {
  getProducts: (request: ProductShopRequest) => Promise<PageResponse<ProductResponse>>;
  searchProducts: (request: ProductSearchRequest) => Promise<PageResponse<ProductResponse>>;
}

export const productService: ProductService = {
  getProducts: async (request) => {
    const { category, ...restParams } = request;

    const categoryParam = category === "ALL" || !category ? undefined : category; 
     
    const response = await api.get<PageResponse<ProductResponse>>("/api/products", {
      params: {
        category: categoryParam,
        ...restParams
      }
    });
    
    return response.data;
  },

  searchProducts: async (request) => {
    const response = await api.get<PageResponse<ProductResponse>>("/api/products/search", {
      params: request
    });
    
    return response.data;
  }
};
