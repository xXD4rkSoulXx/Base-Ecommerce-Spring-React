import { useQuery } from '@tanstack/react-query'
import { orderService } from '@/services/order.service'

export const useOrder = (id: number | null) =>
  useQuery({
    queryKey: ['order', id],
    queryFn: () => orderService.getOrder(id!),
    enabled: id !== null
  })
