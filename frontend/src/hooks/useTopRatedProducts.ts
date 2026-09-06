import { useQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useTopRatedProducts = () => {
  return useQuery({
    queryKey: ['products-top-rated'],
    queryFn: () => productService.getTopRatedProducts()
  })
}
