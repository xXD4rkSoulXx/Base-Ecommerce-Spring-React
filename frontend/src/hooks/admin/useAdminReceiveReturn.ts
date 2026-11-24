import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminReturnService } from '@/services/admin/adminReturn.service'

export const useAdminReceiveReturn = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => adminReturnService.receive(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.returns.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.order.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.products.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.productSkus.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.stock.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
