import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminProductService } from '@/services/admin/adminProduct.service'
import type { AdminProductRequest } from '@/types/admin/adminProduct'

export const useAdminUpdateProduct = (id: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: AdminProductRequest) => adminProductService.updateProduct(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.product(id) })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.products.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.stock.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.reviews.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
