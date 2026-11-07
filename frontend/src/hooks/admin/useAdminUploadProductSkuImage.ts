import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductSkuImageService } from '@/services/admin/adminProductSkuImage.service'

export const useAdminUploadProductSkuImage = (productId: number, skuId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (file: File) => adminProductSkuImageService.uploadImage(productId, skuId, file),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'product-sku-images'], productId, skuId] })
      queryClient.invalidateQueries({ queryKey: [...['admin', 'product-skus'], productId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'product'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
    }
  })
}
