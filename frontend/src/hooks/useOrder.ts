import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { orderService } from '@/services/order.service'

export const useOrder = (id: number | null) =>
  useQuery({
    queryKey: queryKeys.order(id),
    queryFn: () => orderService.getOrder(id!),
    enabled: id !== null
  })
