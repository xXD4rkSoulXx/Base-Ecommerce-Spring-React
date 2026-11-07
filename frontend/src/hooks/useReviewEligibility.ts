import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '@/store/useAuthStore'
import { reviewService } from '@/services/review.service'

export const useReviewEligibility = (id: string | undefined) => {
  const isUserAuthenticated = useAuthStore((state) => !!state.user)

  return useQuery({
    queryKey: ['review-eligibility', id],
    queryFn: () => reviewService.getReviewEligibility(id!),
    enabled: !!id && isUserAuthenticated
  })
}
