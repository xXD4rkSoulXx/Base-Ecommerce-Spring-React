import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminOrderService } from '@/services/admin/adminOrder.service'

export const useAdminOrder = (id: number) =>
  useQuery({
    queryKey: queryKeys.admin.order.detail(id),
    queryFn: () => adminOrderService.getOrder(id)
  })
