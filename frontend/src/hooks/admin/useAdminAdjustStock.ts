import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminStockService } from '@/services/admin/adminStock.service'

interface AdjustStockVariables {
  skuId: number
  delta: number
}

export const useAdminAdjustStock = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ skuId, delta }: AdjustStockVariables) => adminStockService.adjustStock(skuId, { delta }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'product-skus'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
