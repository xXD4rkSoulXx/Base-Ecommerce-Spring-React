import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminReviewService } from '@/services/admin/adminReview.service'

interface AdminSetReviewHiddenVariables {
  id: number
  hidden: boolean
}

export const useAdminSetReviewHidden = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, hidden }: AdminSetReviewHiddenVariables) =>
      hidden ? adminReviewService.hideReview(id) : adminReviewService.showReview(id),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'reviews'] })
    }
  })
}
