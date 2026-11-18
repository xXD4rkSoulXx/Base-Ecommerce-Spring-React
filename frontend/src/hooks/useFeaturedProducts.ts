import { useQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useFeaturedProducts = () =>
  useQuery({
    queryKey: ['products-featured'],
    queryFn: () => productService.getFeaturedProducts()
  })
