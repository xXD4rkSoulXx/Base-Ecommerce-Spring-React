import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductService } from '@/services/admin/adminProduct.service'
import type { AdminProductRequest } from '@/types/admin/adminProduct'

export const useAdminCreateProduct = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: AdminProductRequest) => adminProductService.createProduct(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
