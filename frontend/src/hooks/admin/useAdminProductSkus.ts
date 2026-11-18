import { useQuery } from '@tanstack/react-query'
import { adminProductSkuService } from '@/services/admin/adminProductSku.service'

export const useAdminProductSkus = (productId: number) =>
  useQuery({
    queryKey: ['admin', 'product-skus', productId],
    queryFn: () => adminProductSkuService.getSkus(productId)
  })