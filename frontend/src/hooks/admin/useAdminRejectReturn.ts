import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminReturnService } from '@/services/admin/adminReturn.service'

interface RejectReturnVariables {
  id: number
  reason: string
}

export const useAdminRejectReturn = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, reason }: RejectReturnVariables) => adminReturnService.reject(id, { reason }),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'returns'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'order'] })
    }
  })
}
