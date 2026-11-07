import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminProductService } from '@/services/admin/adminProduct.service'

export const useAdminArchiveProduct = (productId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => adminProductService.archiveProduct(productId),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'product'], productId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
    }
  })
}
