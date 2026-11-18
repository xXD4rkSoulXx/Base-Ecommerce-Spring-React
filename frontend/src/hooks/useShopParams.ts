import { useSearchParams } from 'react-router-dom'
import { ALL_FILTER, DEFAULT_SORT, PRODUCT_PAGE_SIZE } from '@/constants'
import { usePageParams } from '@/hooks/usePageParams'
import type { ProductCategory, ProductSort } from '@/types/product'

export const useShopParams = () => {
  const [searchParams] = useSearchParams()
  const { size } = usePageParams(PRODUCT_PAGE_SIZE)
  const category = (searchParams.get('category') || ALL_FILTER) as ProductCategory | typeof ALL_FILTER
  const sortBy = (searchParams.get('sort') || DEFAULT_SORT) as ProductSort

  return { category, sortBy, size }
}
