import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { adminProductService } from '@/services/admin/adminProduct.service'
import type { ProductCategory } from '@/types/product'

export const useAdminProducts = () => {
  const [searchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const category = (searchParams.get('category') || undefined) as ProductCategory | undefined
  const lowStock = searchParams.get('lowStock') === 'true'
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 10
  const displayPage = urlPage < 1 ? 1 : urlPage

  return useQuery({
    queryKey: [...['admin', 'products'], { q, category, lowStock, page: displayPage, size }],
    queryFn: () => adminProductService.getProducts({
      q: q || undefined,
      category,
      lowStock,
      page: displayPage - 1,
      size
    }),
    placeholderData: (previousData) => previousData
  })
}
