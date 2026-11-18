import { api } from '@/lib/api'
import type {
  AdminAdjustStockRequest,
  AdminStockItemResponse,
  AdminStockSearchRequest,
  AdminSetStockRequest
} from '@/types/admin/adminStock'
import type { PageResponse } from '@/types/page'

export const adminStockService = {
  adjustStock: async (skuId: number, request: AdminAdjustStockRequest) => {
    await api.post(`/api/admin/stock/${skuId}/adjustments`, request)
  },

  getStock: async (request: AdminStockSearchRequest) => {
    const response = await api.get<PageResponse<AdminStockItemResponse>>('/api/admin/stock', {
      params: request
    })

    return response.data
  },

  setStock: async (skuId: number, request: AdminSetStockRequest) => {
    await api.put(`/api/admin/stock/${skuId}`, request)
  }
}
