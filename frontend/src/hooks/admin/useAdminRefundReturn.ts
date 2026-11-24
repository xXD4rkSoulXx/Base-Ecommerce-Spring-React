import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminReturnService } from '@/services/admin/adminReturn.service'

export const useAdminRefundReturn = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => adminReturnService.refund(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.returns.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.orders.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.order.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.customerOrders.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
