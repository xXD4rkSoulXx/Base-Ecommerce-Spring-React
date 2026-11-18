import { api } from '@/lib/api'
import type {
  AdminProductDetailsResponse,
  AdminProductRequest,
  AdminProductSearchRequest,
  AdminProductSummaryResponse
} from '@/types/admin/adminProduct'
import type { PageResponse } from '@/types/page'

export const adminProductService = {
  createProduct: async (request: AdminProductRequest) => {
    const response = await api.post<AdminProductDetailsResponse>('/api/admin/products', request)

    return response.data
  },

  archiveProduct: async (id: number) => {
    await api.post(`/api/admin/products/${id}/archive`)
  },

  restoreProduct: async (id: number) => {
    await api.post(`/api/admin/products/${id}/restore`)
  },

  getProducts: async (request: AdminProductSearchRequest) => {
    const response = await api.get<PageResponse<AdminProductSummaryResponse>>('/api/admin/products', {
      params: request
    })

    return response.data
  },

  getProduct: async (id: number) => {
    const response = await api.get<AdminProductDetailsResponse>(`/api/admin/products/${id}`)

    return response.data
  },

  updateProduct: async (id: number, request: AdminProductRequest) => {
    const response = await api.put<AdminProductDetailsResponse>(`/api/admin/products/${id}`, request)

    return response.data
  }
}
