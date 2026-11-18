import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { PRODUCT_PAGE_SIZE } from '@/constants'
import { usePageParams } from '@/hooks/usePageParams'
import { productService } from '@/services/product.service'

export const useSearchProducts = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams(PRODUCT_PAGE_SIZE)
  const q = searchParams.get('q') || ''

  return useQuery({
    queryKey: ['products-search', { q, page, size }],
    queryFn: () =>
      productService.searchProducts({
        q,
        page: page - 1,
        size
      }),
    enabled: !!q,
    placeholderData: (previousData) => previousData
  })
}
