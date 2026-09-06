import { useQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useNewArrivals = () => {
  return useQuery({
    queryKey: ['products-new-arrivals'],
    queryFn: () => productService.getProducts({
      category: 'ALL',
      page: 0,
      size: 8,
      sortBy: 'recent'
    })
  })
}
