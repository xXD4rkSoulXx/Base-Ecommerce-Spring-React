import { api } from '@/lib/api'
import type { AdminProductSkuResponse, AdminCreateProductSkuRequest, AdminUpdateProductSkuRequest } from '@/types/admin/adminProductSku'

export const adminProductSkuService = {
  createSku: async (productId: number, request: AdminCreateProductSkuRequest) => {
    const response = await api.post<AdminProductSkuResponse>(`/api/admin/products/${productId}/skus`, request)

    return response.data
  },

  archiveSku: async (productId: number, skuId: number) => {
    await api.post(`/api/admin/products/${productId}/skus/${skuId}/archive`)
  },

  restoreSku: async (productId: number, skuId: number) => {
    await api.post(`/api/admin/products/${productId}/skus/${skuId}/restore`)
  },

  getSkus: async (productId: number) => {
    const response = await api.get<AdminProductSkuResponse[]>(`/api/admin/products/${productId}/skus`)

    return response.data
  },

  updateSku: async (productId: number, skuId: number, request: AdminUpdateProductSkuRequest) => {
    const response = await api.put<AdminProductSkuResponse>(`/api/admin/products/${productId}/skus/${skuId}`, request)

    return response.data
  }
}
