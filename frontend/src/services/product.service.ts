import { api } from '@/lib/api'
import type { ProductShopRequest, ProductSearchRequest, ProductResponse, PageResponse, ProductDetailsResponse, ProductReviewsRequest } from '@/types/product'
import type { ReviewResponse } from '@/types/review'

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

  getProductDetails: async (id: number | string) => {
    const response = await api.get<ProductDetailsResponse>(`/api/products/${id}`)

    return response.data
  },

  getRelatedProducts: async (id: number | string) => {
    const response = await api.get<ProductResponse[]>(`/api/products/${id}/related`)

    return response.data
  },

  getProductReviews: async (id: number | string, request: ProductReviewsRequest) => {
    const response = await api.get<PageResponse<ReviewResponse>>(`/api/products/${id}/reviews`, {
      params: request
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
