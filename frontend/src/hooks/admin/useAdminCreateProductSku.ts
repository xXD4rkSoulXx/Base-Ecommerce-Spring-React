import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductSkuService } from '@/services/admin/adminProductSku.service'
import type { AdminCreateProductSkuRequest } from '@/types/admin/adminProductSku'

export const useAdminCreateProductSku = (productId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: AdminCreateProductSkuRequest) =>
        adminProductSkuService.createSku(productId, request),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'product-skus'], productId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
    }
  })
}
