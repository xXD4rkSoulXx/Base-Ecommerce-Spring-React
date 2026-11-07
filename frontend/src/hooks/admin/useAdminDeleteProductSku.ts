import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductSkuService } from '@/services/admin/adminProductSku.service'

export const useAdminDeleteProductSku = (productId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (skuId: number) => adminProductSkuService.deleteSku(productId, skuId),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'product-skus'], productId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
    }
  })
}
