import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminOrderService } from '@/services/admin/order.service'
import type { OrderStatus } from '@/types/order'

export const useChangeOrderStatus = (id: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (status: OrderStatus) => adminOrderService.changeStatus(id, { status }),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'order'], id] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders'] })
    }
  })
}
