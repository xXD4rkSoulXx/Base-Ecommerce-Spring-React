import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { usePageParams } from '@/hooks/usePageParams'
import { adminStockService } from '@/services/admin/adminStock.service'

export const useAdminStock = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams(10)
  const q = searchParams.get('q') || ''
  const lowStock = searchParams.get('lowStock') === 'true'

  return useQuery({
    queryKey: ['admin', 'stock', { q, lowStock, page, size }],
    queryFn: () =>
      adminStockService.getStock({
        q: q || undefined,
        lowStock,
        page: page - 1,
        size
      }),
    placeholderData: (previousData) => previousData
  })
}
