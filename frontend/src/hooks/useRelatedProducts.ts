import { useQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useRelatedProducts = (id: string | undefined) => {
  return useQuery({
    queryKey: ['product-related', id],
    queryFn: () => productService.getRelatedProducts(id!),
    enabled: !!id
  })
}
