import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminProductSkuService } from '@/services/admin/adminProductSku.service'

export const useAdminProductSkus = (productId: number) =>
  useQuery({
    queryKey: queryKeys.admin.productSkus.byProduct(productId),
    queryFn: () => adminProductSkuService.getSkus(productId)
  })
