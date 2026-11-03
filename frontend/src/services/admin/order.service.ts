import { api } from '@/lib/api'
import type { AdminOrderDetailsResponse, AdminOrderSearchRequest, AdminOrderSummaryResponse, UpdateOrderStatusRequest, UpdateShipmentStatusRequest } from '@/types/admin/order'
import type { PageResponse } from '@/types/page'

export const adminOrderService = {
  getOrders: async (request: AdminOrderSearchRequest) => {
    const response = await api.get<PageResponse<AdminOrderSummaryResponse>>('/api/admin/orders', {
      params: request
    })

    return response.data
  },

  getOrder: async (id: number) => {
    const response = await api.get<AdminOrderDetailsResponse>(`/api/admin/orders/${id}`)

    return response.data
  },

  changeStatus: async (id: number, request: UpdateOrderStatusRequest) => {
    await api.put(`/api/admin/orders/${id}/status`, request)
  },

  changeShipmentStatus: async (orderId: number, shipmentId: number, request: UpdateShipmentStatusRequest) => {
    await api.put(`/api/admin/orders/${orderId}/shipments/${shipmentId}/status`, request)
  }
}
