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
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'stock'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'products'] })
    }
  })
}
