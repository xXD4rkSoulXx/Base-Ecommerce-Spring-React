import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminOrderService } from '@/services/admin/adminOrder.service'
import type { OrderStatus } from '@/types/order'

export const useAdminChangeOrderStatus = (id: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (status: OrderStatus) => adminOrderService.changeStatus(id, { status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.order.detail(id) })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.orders.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.customerOrders.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
