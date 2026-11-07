import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminOrderService } from '@/services/admin/adminOrder.service'
import type { OrderStatus } from '@/types/order'

export const useAdminChangeOrderStatus = (id: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (status: OrderStatus) => adminOrderService.changeStatus(id, { status }),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'order'], id] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders'] })
    }
  })
}
