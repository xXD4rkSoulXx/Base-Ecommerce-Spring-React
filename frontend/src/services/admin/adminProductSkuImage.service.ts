import { api } from '@/lib/api'
import type { AdminProductSkuImageResponse } from '@/types/admin/adminProductSkuImage'

const imagesUrl = (productId: number, skuId: number) =>
  `/api/admin/products/${productId}/skus/${skuId}/images`

export const adminProductSkuImageService = {
  uploadImage: async (productId: number, skuId: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)

    const response = await api.post<AdminProductSkuImageResponse>(imagesUrl(productId, skuId), formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    return response.data
  },

  getImages: async (productId: number, skuId: number) => {
    const response = await api.get<AdminProductSkuImageResponse[]>(imagesUrl(productId, skuId))

    return response.data
  },

  setCover: async (productId: number, skuId: number, imageId: number) => {
    await api.put(`${imagesUrl(productId, skuId)}/${imageId}/cover`)
  },

  deleteImage: async (productId: number, skuId: number, imageId: number) => {
    await api.delete(`${imagesUrl(productId, skuId)}/${imageId}`)
  }
}
