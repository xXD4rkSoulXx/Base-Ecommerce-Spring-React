import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { usePageParams } from '@/hooks/usePageParams'
import { orderService } from '@/services/order.service'

const toInstant = (date: string, time: string) => {
  if (!date) {
    return undefined
  }

  const parsed = new Date(`${date}T${time}`)

  return Number.isNaN(parsed.getTime()) ? undefined : parsed.toISOString()
}

export const useOrders = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams()
  const q = searchParams.get('q') || ''
  const from = searchParams.get('from') || ''
  const to = searchParams.get('to') || ''

  return useQuery({
    queryKey: ['orders', { q, from, to, page, size }],
    queryFn: () =>
      orderService.getOrders({
        q: q || undefined,
        from: toInstant(from, '00:00:00'),
        to: toInstant(to, '23:59:59.999'),
        page: page - 1,
        size
      }),
    placeholderData: (previousData) => previousData
  })
}
