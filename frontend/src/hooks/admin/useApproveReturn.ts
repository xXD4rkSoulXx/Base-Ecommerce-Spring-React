import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminReturnService } from '@/services/admin/return.service'

export const useApproveReturn = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => adminReturnService.approve(id),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'returns'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'order'] })
    }
  })
}
