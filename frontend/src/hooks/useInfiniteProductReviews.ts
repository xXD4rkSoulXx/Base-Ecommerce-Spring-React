import { useParams } from 'react-router-dom'
import { useInfiniteQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useInfiniteProductReviews = () => {
  const { id } = useParams<{ id: string }>()
  const size = 5

  return useInfiniteQuery({
    queryKey: ['product-reviews-infinite', id],
    queryFn: ({ pageParam }) => productService.getProductReviews(id!, {
      page: pageParam,
      size
    }),
    initialPageParam: 0,
    enabled: !!id,
    getNextPageParam: (lastPage) => {
      if (lastPage.last) {
        return undefined
      }

      return lastPage.page + 1
    }
  })
}
