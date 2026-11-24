import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { productService } from '@/services/product.service'

export const useBestSellers = () =>
  useQuery({
    queryKey: queryKeys.bestSellers,
    queryFn: () => productService.getBestSellers()
  })
