import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductSkuService } from '@/services/admin/adminProductSku.service'
import type { AdminUpdateProductSkuRequest } from '@/types/admin/adminProductSku'

interface UpdateProductSkuVariables {
  skuId: number
  request: AdminUpdateProductSkuRequest
}

export const useAdminUpdateProductSku = (productId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ skuId, request }: UpdateProductSkuVariables) => adminProductSkuService.updateSku(productId, skuId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'product-skus', productId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}