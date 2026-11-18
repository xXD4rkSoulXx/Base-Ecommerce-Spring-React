import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { usePageParams } from '@/hooks/usePageParams'
import { adminCustomerService } from '@/services/admin/adminCustomer.service'

export const useAdminCustomers = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams()
  const q = searchParams.get('q') || ''

  return useQuery({
    queryKey: ['admin', 'customers', { q, page, size }],
    queryFn: () =>
      adminCustomerService.getCustomers({
        q: q || undefined,
        page: page - 1,
        size
      }),
    placeholderData: (previousData) => previousData
  })
}
