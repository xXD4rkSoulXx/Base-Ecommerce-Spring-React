import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { adminCustomerService } from '@/services/admin/adminCustomer.service'

export const useAdminCustomers = () => {
  const [searchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 10
  const displayPage = urlPage < 1 ? 1 : urlPage

  return useQuery({
    queryKey: [...['admin', 'customers'], { q, page: displayPage, size }],
    queryFn: () => adminCustomerService.getCustomers({
      q: q || undefined,
      page: displayPage - 1,
      size
    }),
    placeholderData: (previousData) => previousData
  })
}
