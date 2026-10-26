import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { orderService } from '@/services/order.service'

const toInstant = (date: string, time: string) => {
  const parsed = new Date(`${date}T${time}`)

  return date && !Number.isNaN(parsed.getTime()) ? parsed.toISOString() : undefined
}

export const useOrders = () => {
  const [searchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const from = searchParams.get('from') || ''
  const to = searchParams.get('to') || ''
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 10
  const displayPage = urlPage < 1 ? 1 : urlPage
  const apiPage = displayPage - 1

  return useQuery({
    queryKey: ['orders', { q, from, to, page: displayPage, size }],
    queryFn: () => orderService.getOrders({
      q: q || undefined,
      from: toInstant(from, '00:00:00'),
      to: toInstant(to, '23:59:59.999'),
      page: apiPage,
      size
    }),
    placeholderData: (previousData) => previousData
  })
}
