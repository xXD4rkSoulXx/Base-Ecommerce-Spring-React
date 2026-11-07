import { useQuery } from '@tanstack/react-query'
import { adminProductService } from '@/services/admin/adminProduct.service'

export const useAdminProduct = (id: number) => {
  return useQuery({
    queryKey: [...['admin', 'product'], id],
    queryFn: () => adminProductService.getProduct(id)
  })
}
