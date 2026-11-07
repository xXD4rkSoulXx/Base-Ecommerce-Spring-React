import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductService } from '@/services/admin/adminProduct.service'
import type { AdminProductRequest } from '@/types/admin/adminProduct'

export const useAdminCreateProduct = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: AdminProductRequest) => adminProductService.createProduct(request),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
    }
  })
}
