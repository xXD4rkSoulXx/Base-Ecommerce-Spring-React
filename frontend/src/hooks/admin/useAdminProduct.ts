import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminProductService } from '@/services/admin/adminProduct.service'

export const useAdminProduct = (id: number) =>
  useQuery({
    queryKey: queryKeys.admin.product(id),
    queryFn: () => adminProductService.getProduct(id)
  })
