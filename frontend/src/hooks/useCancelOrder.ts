import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { orderService } from '@/services/order.service'

export const useCancelOrder = (orderId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => orderService.cancelOrder(orderId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.order(orderId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.all })
    }
  })
}
