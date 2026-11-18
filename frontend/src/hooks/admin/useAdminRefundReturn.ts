import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminReturnService } from '@/services/admin/adminReturn.service'

export const useAdminRefundReturn = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => adminReturnService.refund(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'returns'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'order'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'customer-orders'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
