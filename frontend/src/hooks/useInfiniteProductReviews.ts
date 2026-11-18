import { useInfiniteQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

const REVIEWS_PAGE_SIZE = 5

export const useInfiniteProductReviews = (id: string | undefined) =>
  useInfiniteQuery({
    queryKey: ['product-reviews-infinite', id],
    queryFn: ({ pageParam }) =>
      productService.getProductReviews(id!, {
        page: pageParam,
        size: REVIEWS_PAGE_SIZE
      }),
    initialPageParam: 0,
    enabled: !!id,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1)
  })
