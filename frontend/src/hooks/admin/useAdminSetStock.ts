import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminStockService } from '@/services/admin/adminStock.service'

interface SetStockVariables {
  skuId: number
  stock: number
}

export const useAdminSetStock = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ skuId, stock }: SetStockVariables) => adminStockService.setStock(skuId, { stock }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'product-skus'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
