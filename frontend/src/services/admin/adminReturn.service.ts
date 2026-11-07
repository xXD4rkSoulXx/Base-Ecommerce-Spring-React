import { api } from '@/lib/api'
import type { AdminReturnResponse, AdminReturnSearchRequest, AdminRejectReturnRequest } from '@/types/admin/adminReturn'
import type { PageResponse } from '@/types/page'

export const adminReturnService = {
  approve: async (id: number) => {
    const response = await api.post<AdminReturnResponse>(`/api/admin/returns/${id}/approve`)

    return response.data
  },

  reject: async (id: number, request: AdminRejectReturnRequest) => {
    const response = await api.post<AdminReturnResponse>(`/api/admin/returns/${id}/reject`, request)

    return response.data
  },

  receive: async (id: number) => {
    const response = await api.post<AdminReturnResponse>(`/api/admin/returns/${id}/receive`)

    return response.data
  },

  refund: async (id: number) => {
    const response = await api.post<AdminReturnResponse>(`/api/admin/returns/${id}/refund`)

    return response.data
  },

  getReturns: async (request: AdminReturnSearchRequest) => {
    const response = await api.get<PageResponse<AdminReturnResponse>>('/api/admin/returns', {
      params: request
    })

    return response.data
  }
}
