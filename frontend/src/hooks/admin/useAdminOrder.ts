import { useQuery } from '@tanstack/react-query'
import { adminOrderService } from '@/services/admin/adminOrder.service'

export const useAdminOrder = (id: number) => {
  return useQuery({
    queryKey: [...['admin', 'order'], id],
    queryFn: () => adminOrderService.getOrder(id)
  })
}
