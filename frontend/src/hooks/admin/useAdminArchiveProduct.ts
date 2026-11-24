import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminProductService } from '@/services/admin/adminProduct.service'

export const useAdminArchiveProduct = (productId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => adminProductService.archiveProduct(productId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.product(productId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.products.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.stock.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
