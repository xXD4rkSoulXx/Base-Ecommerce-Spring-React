import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminProductSkuService } from '@/services/admin/adminProductSku.service'

export const useAdminRestoreProductSku = (productId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (skuId: number) => adminProductSkuService.restoreSku(productId, skuId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.productSkus.byProduct(productId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.products.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.stock.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
