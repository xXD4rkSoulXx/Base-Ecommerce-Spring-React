import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminProductSkuService } from '@/services/admin/adminProductSku.service'
import type { AdminCreateProductSkuRequest } from '@/types/admin/adminProductSku'

export const useAdminCreateProductSku = (productId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: AdminCreateProductSkuRequest) => adminProductSkuService.createSku(productId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.productSkus.byProduct(productId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.products.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.stock.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
