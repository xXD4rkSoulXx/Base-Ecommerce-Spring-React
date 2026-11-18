import { useQuery } from '@tanstack/react-query'
import { usePageParams } from '@/hooks/usePageParams'
import { adminCustomerService } from '@/services/admin/adminCustomer.service'

export const useAdminCustomerOrders = (id: number) => {
  const { page, size } = usePageParams(10)

  return useQuery({
    queryKey: ['admin', 'customer-orders', id, { page, size }],
    queryFn: () => adminCustomerService.getCustomerOrders(id, { page: page - 1, size }),
    placeholderData: (previousData) => previousData
  })
}
