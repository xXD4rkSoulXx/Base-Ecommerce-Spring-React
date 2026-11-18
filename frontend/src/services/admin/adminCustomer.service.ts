import { api } from '@/lib/api'
import type { AdminCustomerOrderSearchRequest, AdminCustomerResponse, AdminCustomerSearchRequest } from '@/types/admin/adminCustomer'
import type { OrderSummaryResponse } from '@/types/order'
import type { PageResponse } from '@/types/page'

export const adminCustomerService = {
  getCustomers: async (request: AdminCustomerSearchRequest) => {
    const response = await api.get<PageResponse<AdminCustomerResponse>>('/api/admin/customers', {
      params: request
    })

    return response.data
  },

  getCustomer: async (id: number) => {
    const response = await api.get<AdminCustomerResponse>(`/api/admin/customers/${id}`)

    return response.data
  },

  getCustomerOrders: async (id: number, request: AdminCustomerOrderSearchRequest) => {
    const response = await api.get<PageResponse<OrderSummaryResponse>>(`/api/admin/customers/${id}/orders`, {
      params: request
    })

    return response.data
  }
}
