import { api } from '@/lib/api'
import type {
  AdminDiscountCodeResponse,
  AdminDiscountCodeSearchRequest,
  AdminCreateDiscountCodeRequest,
  AdminUpdateDiscountCodeRequest
} from '@/types/admin/adminDiscountCode'
import type { PageResponse } from '@/types/page'

export const adminDiscountCodeService = {
  createDiscountCode: async (request: AdminCreateDiscountCodeRequest) => {
    const response = await api.post<AdminDiscountCodeResponse>('/api/admin/discount-codes', request)

    return response.data
  },

  activateDiscountCode: async (id: number) => {
    await api.post(`/api/admin/discount-codes/${id}/activate`)
  },

  deactivateDiscountCode: async (id: number) => {
    await api.post(`/api/admin/discount-codes/${id}/deactivate`)
  },

  getDiscountCodes: async (request: AdminDiscountCodeSearchRequest) => {
    const response = await api.get<PageResponse<AdminDiscountCodeResponse>>('/api/admin/discount-codes', {
      params: request
    })

    return response.data
  },

  updateDiscountCode: async (id: number, request: AdminUpdateDiscountCodeRequest) => {
    const response = await api.put<AdminDiscountCodeResponse>(`/api/admin/discount-codes/${id}`, request)

    return response.data
  }
}
