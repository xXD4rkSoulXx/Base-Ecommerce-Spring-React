import { useInfiniteQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { productService } from '@/services/product.service'
import type { ProductCategory } from '@/types/product'

export const useInfiniteProducts = () => {
  const [searchParams] = useSearchParams()
  const category = (searchParams.get('category') || 'ALL') as ProductCategory | 'ALL'
  const size = Number(searchParams.get('size')) || 12
  const sortBy = searchParams.get('sort') || 'recent'

  return useInfiniteQuery({
    queryKey: ['products', { category, size, sortBy }],
    queryFn: ({ pageParam }) => productService.getProducts({
      category,
      page: pageParam,
      size,
      sortBy
    }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (lastPage.last) {
        return undefined
      }

      return lastPage.page + 1
    }
  })
}
