import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminProductSkuImageService } from '@/services/admin/adminProductSkuImage.service'

export const useAdminProductSkuImages = (productId: number, skuId: number) =>
  useQuery({
    queryKey: queryKeys.admin.productSkuImages.bySku(productId, skuId),
    queryFn: () => adminProductSkuImageService.getImages(productId, skuId)
  })
