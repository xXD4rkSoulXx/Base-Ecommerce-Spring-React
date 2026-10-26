import { useQuery } from '@tanstack/react-query'
import { orderService } from '@/services/order.service'

export const useOrder = (id: number | null) => {
  return useQuery({
    queryKey: ['order', id],
    queryFn: () => orderService.getOrder(id as number),
    enabled: id !== null
  })
}
