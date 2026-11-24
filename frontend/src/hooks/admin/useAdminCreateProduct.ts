import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminProductService } from '@/services/admin/adminProduct.service'
import type { AdminProductRequest } from '@/types/admin/adminProduct'

export const useAdminCreateProduct = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: AdminProductRequest) => adminProductService.createProduct(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.products.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
