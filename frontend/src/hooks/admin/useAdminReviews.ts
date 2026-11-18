import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { usePageParams } from '@/hooks/usePageParams'
import { adminReviewService } from '@/services/admin/adminReview.service'

export const useAdminReviews = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams()
  const q = searchParams.get('q') || ''
  const hiddenParam = searchParams.get('hidden')
  const hidden = hiddenParam ? hiddenParam === 'true' : undefined

  return useQuery({
    queryKey: ['admin', 'reviews', { q, hidden, page, size }],
    queryFn: () =>
      adminReviewService.getReviews({
        q: q || undefined,
        hidden,
        page: page - 1,
        size
      }),
    placeholderData: (previousData) => previousData
  })
}
