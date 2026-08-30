import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { reviewService } from '@/services/review.service'
import { useAuthStore } from '@/store/useAuthStore'

export const useReviewEligibility = () => {
  const { id } = useParams<{ id: string }>()
  const isUserAuthenticated = useAuthStore((state) => !!state.user)

  return useQuery({
    queryKey: ['review-eligibility', id],
    queryFn: () => reviewService.getReviewEligibility(id!),
    enabled: !!id && isUserAuthenticated
  })
}
