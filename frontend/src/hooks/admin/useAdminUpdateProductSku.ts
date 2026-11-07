import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductSkuService } from '@/services/admin/adminProductSku.service'
import type { AdminUpdateProductSkuRequest } from '@/types/admin/adminProductSku'

interface AdminUpdateProductSkuVariables {
  skuId: number
  request: AdminUpdateProductSkuRequest
}

export const useAdminUpdateProductSku = (productId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ skuId, request }: AdminUpdateProductSkuVariables) =>
      adminProductSkuService.updateSku(productId, skuId, request),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'product-skus'], productId] })
    }
  })
}
