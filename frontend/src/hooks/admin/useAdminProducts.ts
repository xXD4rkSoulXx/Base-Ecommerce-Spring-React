import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { usePageParams } from '@/hooks/usePageParams'
import { adminProductService } from '@/services/admin/adminProduct.service'
import type { ProductCategory } from '@/types/product'

export const useAdminProducts = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams()
  const q = searchParams.get('q') || ''
  const category = (searchParams.get('category') || undefined) as ProductCategory | undefined
  const lowStock = searchParams.get('lowStock') === 'true'

  return useQuery({
    queryKey: ['admin', 'products', { q, category, lowStock, page, size }],
    queryFn: () =>
      adminProductService.getProducts({
        q: q || undefined,
        category,
        lowStock,
        page: page - 1,
        size
      }),
    placeholderData: (previousData) => previousData
  })
}
