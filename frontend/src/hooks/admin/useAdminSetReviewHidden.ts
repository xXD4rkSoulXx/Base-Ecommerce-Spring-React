import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminReviewService } from '@/services/admin/adminReview.service'

interface SetReviewHiddenVariables {
  id: number
  hidden: boolean
}

export const useAdminSetReviewHidden = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, hidden }: SetReviewHiddenVariables) =>
      hidden ? adminReviewService.hideReview(id) : adminReviewService.showReview(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.reviews.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
