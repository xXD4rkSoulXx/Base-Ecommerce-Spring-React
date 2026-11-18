import { api } from '@/lib/api'
import type { AdminProductSkuImageResponse } from '@/types/admin/adminProductSkuImage'

export const adminProductSkuImageService = {
  uploadImage: async (productId: number, skuId: number, file: File) => {
    const formData = new FormData()

    formData.append('file', file)

    const response = await api.post<AdminProductSkuImageResponse>(
      `/api/admin/products/${productId}/skus/${skuId}/images`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    )

    return response.data
  },

  getImages: async (productId: number, skuId: number) => {
    const response = await api.get<AdminProductSkuImageResponse[]>(`/api/admin/products/${productId}/skus/${skuId}/images`)

    return response.data
  },

  setSkuCover: async (productId: number, skuId: number, imageId: number) => {
    await api.put(`/api/admin/products/${productId}/skus/${skuId}/images/${imageId}/sku-cover`)
  },

  setProductCover: async (productId: number, skuId: number, imageId: number) => {
    await api.put(`/api/admin/products/${productId}/skus/${skuId}/images/${imageId}/product-cover`)
  },

  deleteImage: async (productId: number, skuId: number, imageId: number) => {
    await api.delete(`/api/admin/products/${productId}/skus/${skuId}/images/${imageId}`)
  }
}
