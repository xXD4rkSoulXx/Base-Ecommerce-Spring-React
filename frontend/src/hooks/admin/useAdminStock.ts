import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { adminStockService } from '@/services/admin/adminStock.service'

export const useAdminStock = () => {
  const [searchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const lowStock = searchParams.get('lowStock') === 'true'
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 10
  const displayPage = urlPage < 1 ? 1 : urlPage

  return useQuery({
    queryKey: [...['admin', 'stock'], { q, lowStock, page: displayPage, size }],
    queryFn: () => adminStockService.getStock({
      q: q || undefined,
      lowStock,
      page: displayPage - 1,
      size
    }),
    placeholderData: (previousData) => previousData
  })
}
