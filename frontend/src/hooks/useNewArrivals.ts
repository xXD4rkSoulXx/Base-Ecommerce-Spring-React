import { useQuery } from '@tanstack/react-query'
import { ALL_FILTER, DEFAULT_SORT } from '@/constants'
import { productService } from '@/services/product.service'

export const useNewArrivals = () =>
  useQuery({
    queryKey: ['products-new-arrivals'],
    queryFn: () =>
      productService.getProducts({
        category: ALL_FILTER,
        page: 0,
        size: 8,
        sortBy: DEFAULT_SORT
      }),
    select: (page) => page.content
  })
