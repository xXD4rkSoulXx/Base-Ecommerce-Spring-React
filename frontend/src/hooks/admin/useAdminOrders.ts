import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { ALL_FILTER, TODO_FILTER } from '@/constants'
import { usePageParams } from '@/hooks/usePageParams'
import { adminOrderService } from '@/services/admin/adminOrder.service'
import type { OrderStatus } from '@/types/order'

export const useAdminOrders = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams()
  const q = searchParams.get('q') || ''
  const filter = searchParams.get('status') || TODO_FILTER
  const pending = filter === TODO_FILTER
  const status = pending || filter === ALL_FILTER ? undefined : (filter as OrderStatus)

  return useQuery({
    queryKey: ['admin', 'orders', { q, filter, page, size }],
    queryFn: () =>
      adminOrderService.getOrders({
        q: q || undefined,
        pending,
        status,
        page: page - 1,
        size
      }),
    placeholderData: (previousData) => previousData
  })
}
