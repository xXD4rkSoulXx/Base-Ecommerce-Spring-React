import { useQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useBestSellers = () => {
  return useQuery({
    queryKey: ['best-sellers'],
    queryFn: () => productService.getBestSellers()
  })
}
