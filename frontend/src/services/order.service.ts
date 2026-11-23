import { api } from '@/lib/api'
import type {
  OrderSearchRequest,
  OrderSummaryResponse,
  OrderDetailsResponse,
  CreateReturnRequest,
  OrderReturnResponse
} from '@/types/order'
import type { PageResponse } from '@/types/page'

export const orderService = {
  requestReturn: async (orderId: number, request: CreateReturnRequest) => {
    const response = await api.post<OrderReturnResponse>(`/api/orders/${orderId}/returns`, request)

    return response.data
  },

  cancelOrder: async (orderId: number) => {
    await api.post(`/api/orders/${orderId}/cancel`)
  },

  getOrders: async (request: OrderSearchRequest) => {
    const response = await api.get<PageResponse<OrderSummaryResponse>>('/api/orders', {
      params: request
    })

    return response.data
  },

  getOrder: async (id: number) => {
    const response = await api.get<OrderDetailsResponse>(`/api/orders/${id}`)

    return response.data
  },

  getReturnSlip: async (orderId: number, returnId: number) => {
    const response = await api.get<Blob>(`/api/orders/${orderId}/returns/${returnId}/slip`, {
      responseType: 'blob'
    })

    return response.data
  }
}
