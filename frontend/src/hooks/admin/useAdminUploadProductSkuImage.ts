import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminProductSkuImageService } from '@/services/admin/adminProductSkuImage.service'

export const useAdminUploadProductSkuImage = (productId: number, skuId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (file: File) => adminProductSkuImageService.uploadImage(productId, skuId, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.productSkuImages.bySku(productId, skuId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.productSkus.byProduct(productId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.product(productId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.products.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.stock.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
