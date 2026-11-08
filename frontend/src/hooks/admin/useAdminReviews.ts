import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { adminReviewService } from '@/services/admin/adminReview.service'

export const useAdminReviews = () => {
  const [searchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const hiddenParam = searchParams.get('hidden')
  const hidden = hiddenParam ? hiddenParam === 'true' : undefined
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 10
  const displayPage = urlPage < 1 ? 1 : urlPage

  return useQuery({
    queryKey: [...['admin', 'reviews'], { q, hidden, page: displayPage, size }],
    queryFn: () => adminReviewService.getReviews({
      q: q || undefined,
      hidden,
      page: displayPage - 1,
      size
    }),
    placeholderData: (previousData) => previousData
  })
}
