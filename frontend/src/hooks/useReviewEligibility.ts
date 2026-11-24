import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { useAuthStore } from '@/store/useAuthStore'
import { reviewService } from '@/services/review.service'

export const useReviewEligibility = (id: string | undefined) => {
  const isAuthenticated = useAuthStore((state) => !!state.user)

  return useQuery({
    queryKey: queryKeys.reviewEligibility(id),
    queryFn: () => reviewService.getReviewEligibility(id!),
    enabled: !!id && isAuthenticated
  })
}
