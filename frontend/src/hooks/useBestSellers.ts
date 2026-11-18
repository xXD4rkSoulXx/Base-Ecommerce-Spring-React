import { useQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useBestSellers = () =>
  useQuery({
    queryKey: ['products-best-sellers'],
    queryFn: () => productService.getBestSellers()
  })
