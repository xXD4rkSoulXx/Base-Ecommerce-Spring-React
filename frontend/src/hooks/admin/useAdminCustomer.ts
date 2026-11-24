import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminCustomerService } from '@/services/admin/adminCustomer.service'

export const useAdminCustomer = (id: number) =>
  useQuery({
    queryKey: queryKeys.admin.customer(id),
    queryFn: () => adminCustomerService.getCustomer(id)
  })
