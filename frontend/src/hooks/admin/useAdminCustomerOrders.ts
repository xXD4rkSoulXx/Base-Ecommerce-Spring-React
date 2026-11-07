import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { adminCustomerService } from '@/services/admin/adminCustomer.service'

export const useAdminCustomerOrders = (id: number) => {
  const [searchParams] = useSearchParams()
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 10
  const displayPage = urlPage < 1 ? 1 : urlPage

  return useQuery({
    queryKey: [...['admin', 'customer-orders'], id, { page: displayPage, size }],
    queryFn: () => adminCustomerService.getCustomerOrders(id, { page: displayPage - 1, size }),
    placeholderData: (previousData) => previousData
  })
}
