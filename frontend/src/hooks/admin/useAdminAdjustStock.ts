import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
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
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.stock.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.products.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.productSkus.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
