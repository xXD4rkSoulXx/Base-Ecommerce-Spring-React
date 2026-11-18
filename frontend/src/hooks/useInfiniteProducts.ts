import { useInfiniteQuery } from '@tanstack/react-query'
import { useShopParams } from '@/hooks/useShopParams'
import { productService } from '@/services/product.service'

export const useInfiniteProducts = () => {
  const { category, size, sortBy } = useShopParams()

  return useInfiniteQuery({
    queryKey: ['products', { category, size, sortBy }],
    queryFn: ({ pageParam }) =>
      productService.getProducts({
        category,
        page: pageParam,
        size,
        sortBy
      }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1)
  })
}
