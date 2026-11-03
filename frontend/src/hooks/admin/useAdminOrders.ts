import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { adminOrderService } from '@/services/admin/order.service'
import type { OrderStatus } from '@/types/order'

export const useAdminOrders = () => {
  const [searchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const filter = searchParams.get('status') || 'TODO'
  const pending = filter === 'TODO'
  const status = pending || filter === 'ALL' ? undefined : (filter as OrderStatus)
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 10
  const displayPage = urlPage < 1 ? 1 : urlPage

  return useQuery({
    queryKey: [...['admin', 'orders'], { q, filter, page: displayPage, size }],
    queryFn: () => adminOrderService.getOrders({
      q: q || undefined,
      pending,
      status,
      page: displayPage - 1,
      size
    }),
    placeholderData: (previousData) => previousData
  })
}
