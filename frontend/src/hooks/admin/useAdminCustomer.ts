import { useQuery } from '@tanstack/react-query'
import { adminCustomerService } from '@/services/admin/adminCustomer.service'

export const useAdminCustomer = (id: number) => {
  return useQuery({
    queryKey: [...['admin', 'customer'], id],
    queryFn: () => adminCustomerService.getCustomer(id)
  })
}
