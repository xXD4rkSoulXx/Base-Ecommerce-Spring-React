import { api } from '@/lib/api'
import type { ProductShopRequest, ProductSearchRequest, ProductResponse, PageResponse } from '@/types/product'

export const productService = {
  getProducts: async (request: ProductShopRequest) => {
    const { category, ...restParams } = request
    const categoryParam = category === 'ALL' || !category ? undefined : category
     
    const response = await api.get<PageResponse<ProductResponse>>('/api/products', {
      params: {
        category: categoryParam,
        ...restParams
      }
    })
    
    return response.data
  },

  searchProducts: async (request: ProductSearchRequest) => {
    const response = await api.get<PageResponse<ProductResponse>>('/api/products/search', {
      params: request
    })
    
    return response.data
  }
}
