import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { adminDiscountCodeService } from '@/services/admin/adminDiscountCode.service'

export const useAdminDiscountCodes = () => {
  const [searchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const activeParam = searchParams.get('active')
  const active = activeParam ? activeParam === 'true' : undefined
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 10
  const displayPage = urlPage < 1 ? 1 : urlPage

  return useQuery({
    queryKey: [...['admin', 'discount-codes'], { q, active, page: displayPage, size }],
    queryFn: () => adminDiscountCodeService.getDiscountCodes({
      q: q || undefined,
      active,
      page: displayPage - 1,
      size
    }),
    placeholderData: (previousData) => previousData
  })
}
