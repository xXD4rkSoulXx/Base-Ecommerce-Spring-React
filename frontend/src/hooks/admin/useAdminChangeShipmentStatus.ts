import { useMutation, useQueryClient } from '@tanstack/react-query'
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
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [...['admin', 'order'], orderId] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders'] })
    }
  })
}
