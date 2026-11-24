import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { reviewService } from '@/services/review.service'
import type { ReviewRequest } from '@/types/review'

export const useCreateReview = (id: string | undefined) => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: ReviewRequest) => reviewService.createReview(id!, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.product(id) })
      queryClient.invalidateQueries({ queryKey: queryKeys.productReviews(id) })
      queryClient.invalidateQueries({ queryKey: queryKeys.reviewEligibility(id) })
    }
  })
}
