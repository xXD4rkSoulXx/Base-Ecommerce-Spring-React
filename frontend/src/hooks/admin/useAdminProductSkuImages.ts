import { useQuery } from '@tanstack/react-query'
import { adminProductSkuImageService } from '@/services/admin/adminProductSkuImage.service'

export const useAdminProductSkuImages = (productId: number, skuId: number) =>
  useQuery({
    queryKey: ['admin', 'product-sku-images', productId, skuId],
    queryFn: () => adminProductSkuImageService.getImages(productId, skuId)
  })