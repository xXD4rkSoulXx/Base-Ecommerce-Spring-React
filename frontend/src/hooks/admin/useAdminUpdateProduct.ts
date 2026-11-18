import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductService } from '@/services/admin/adminProduct.service'
import type { AdminProductRequest } from '@/types/admin/adminProduct'

export const useAdminUpdateProduct = (id: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: AdminProductRequest) => adminProductService.updateProduct(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'product', id] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'reviews'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
