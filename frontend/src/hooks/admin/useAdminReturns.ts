import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { ALL_FILTER, TODO_FILTER } from '@/constants'
import { usePageParams } from '@/hooks/usePageParams'
import { adminReturnService } from '@/services/admin/adminReturn.service'
import type { ReturnStatus } from '@/types/order'

export const useAdminReturns = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams()
  const filter = searchParams.get('status') || TODO_FILTER
  const pending = filter === TODO_FILTER
  const status = pending || filter === ALL_FILTER ? undefined : (filter as ReturnStatus)

  return useQuery({
    queryKey: ['admin', 'returns', { filter, page, size }],
    queryFn: () => adminReturnService.getReturns({ pending, status, page: page - 1, size }),
    placeholderData: (previousData) => previousData
  })
}
