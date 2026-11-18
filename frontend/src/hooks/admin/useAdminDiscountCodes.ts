import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { usePageParams } from '@/hooks/usePageParams'
import { adminDiscountCodeService } from '@/services/admin/adminDiscountCode.service'

export const useAdminDiscountCodes = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams()
  const q = searchParams.get('q') || ''
  const activeParam = searchParams.get('active')
  const active = activeParam ? activeParam === 'true' : undefined

  return useQuery({
    queryKey: ['admin', 'discount-codes', { q, active, page, size }],
    queryFn: () =>
      adminDiscountCodeService.getDiscountCodes({
        q: q || undefined,
        active,
        page: page - 1,
        size
      }),
    placeholderData: (previousData) => previousData
  })
}
