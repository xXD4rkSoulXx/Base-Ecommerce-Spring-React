import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
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
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.stock.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.products.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.productSkus.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
