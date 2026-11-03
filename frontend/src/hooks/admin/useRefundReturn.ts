import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminReturnService } from '@/services/admin/return.service'

export const useRefundReturn = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => adminReturnService.refund(id),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'returns'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'order'] })
    }
  })
}
