import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductSkuImageService } from '@/services/admin/adminProductSkuImage.service'

export const useAdminSetProductCover = (productId: number, skuId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (imageId: number) => adminProductSkuImageService.setProductCover(productId, skuId, imageId),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'product-sku-images'], productId] })
      queryClient.invalidateQueries({ queryKey: [...['admin', 'product-skus'], productId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'product'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
    }
  })
}
