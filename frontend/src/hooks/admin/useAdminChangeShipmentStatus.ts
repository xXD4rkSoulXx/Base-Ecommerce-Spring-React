import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminOrderService } from '@/services/admin/adminOrder.service'
import type { ShipmentStatus } from '@/types/order'

interface ChangeShipmentStatusVariables {
  shipmentId: number
  status: ShipmentStatus
}

export const useAdminChangeShipmentStatus = (orderId: number) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ shipmentId, status }: ChangeShipmentStatusVariables) =>
      adminOrderService.changeShipmentStatus(orderId, shipmentId, { status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.order.detail(orderId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.orders.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.customerOrders.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
