import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { adminReturnService } from '@/services/admin/return.service'
import type { ReturnStatus } from '@/types/order'

export const DEFAULT_RETURN_FILTER = 'TODO'

export const useAdminReturns = () => {
  const [searchParams] = useSearchParams()
  const filter = searchParams.get('status') || 'TODO'
  const pending = filter === 'TODO'
  const status = pending || filter === 'ALL' ? undefined : (filter as ReturnStatus)
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 10
  const displayPage = urlPage < 1 ? 1 : urlPage

  return useQuery({
    queryKey: [...['admin', 'returns'], { filter, page: displayPage, size }],
    queryFn: () => adminReturnService.getReturns({ pending, status, page: displayPage - 1, size }),
    placeholderData: (previousData) => previousData
  })
}
