import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductSkuImageService } from '@/services/admin/adminProductSkuImage.service'

export const useAdminUploadProductSkuImage = (productId: number, skuId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (file: File) => adminProductSkuImageService.uploadImage(productId, skuId, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'product-sku-images', productId, skuId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'product-skus', productId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'product', productId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
