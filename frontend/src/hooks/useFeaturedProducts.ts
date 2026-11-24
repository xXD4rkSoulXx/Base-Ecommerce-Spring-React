import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { productService } from '@/services/product.service'

export const useFeaturedProducts = () =>
  useQuery({
    queryKey: queryKeys.featuredProducts,
    queryFn: () => productService.getFeaturedProducts()
  })
