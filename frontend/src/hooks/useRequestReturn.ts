import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { orderService } from '@/services/order.service'
import type { CreateReturnRequest } from '@/types/order'

export const useRequestReturn = (orderId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CreateReturnRequest) => orderService.requestReturn(orderId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.order(orderId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.all })
    }
  })
}
