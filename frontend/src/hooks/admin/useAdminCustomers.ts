import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { usePageParams } from '@/hooks/usePageParams'
import { queryKeys } from '@/queryKeys'
import { adminCustomerService } from '@/services/admin/adminCustomer.service'

export const useAdminCustomers = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams()
  const q = searchParams.get('q') || ''

  return useQuery({
    queryKey: queryKeys.admin.customers.list({ q, page, size }),
    queryFn: () =>
      adminCustomerService.getCustomers({
        q: q || undefined,
        page: page - 1,
        size
      }),
    placeholderData: (previousData) => previousData
  })
}
