import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductService } from '@/services/admin/adminProduct.service'

export const useAdminRestoreProduct = (productId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => adminProductService.restoreProduct(productId),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'product'], productId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
    }
  })
}
