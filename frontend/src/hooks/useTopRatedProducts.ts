import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { productService } from '@/services/product.service'

export const useTopRatedProducts = () =>
  useQuery({
    queryKey: queryKeys.topRated,
    queryFn: () => productService.getTopRatedProducts()
  })
