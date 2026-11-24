import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { productService } from '@/services/product.service'

export const useDealProducts = () =>
  useQuery({
    queryKey: queryKeys.dealProducts,
    queryFn: () => productService.getDealProducts()
  })
