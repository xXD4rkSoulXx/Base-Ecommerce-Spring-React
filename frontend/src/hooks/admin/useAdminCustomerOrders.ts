import { useQuery } from '@tanstack/react-query'
import { usePageParams } from '@/hooks/usePageParams'
import { queryKeys } from '@/queryKeys'
import { adminCustomerService } from '@/services/admin/adminCustomer.service'

export const useAdminCustomerOrders = (id: number) => {
  const { page, size } = usePageParams()

  return useQuery({
    queryKey: queryKeys.admin.customerOrders.list(id, { page, size }),
    queryFn: () => adminCustomerService.getCustomerOrders(id, { page: page - 1, size }),
    placeholderData: (previousData) => previousData
  })
}
