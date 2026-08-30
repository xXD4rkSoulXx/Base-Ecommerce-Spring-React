import { useParams } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { reviewService } from '@/services/review.service'
import type { ReviewRequest } from '@/types/review'

export const useCreateReview = () => {
  const { id } = useParams<{ id: string }>()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: ReviewRequest) => reviewService.createReview(id!, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['product', id] })
      queryClient.invalidateQueries({ queryKey: ['product-reviews-infinite', id] })
      queryClient.invalidateQueries({ queryKey: ['review-eligibility', id] })
    }
  })
}
