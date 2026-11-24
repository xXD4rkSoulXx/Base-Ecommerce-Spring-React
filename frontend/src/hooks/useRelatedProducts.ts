import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { productService } from '@/services/product.service'

export const useRelatedProducts = (id: string | undefined) =>
  useQuery({
    queryKey: queryKeys.productRelated(id),
    queryFn: () => productService.getRelatedProducts(id!),
    enabled: !!id
  })
