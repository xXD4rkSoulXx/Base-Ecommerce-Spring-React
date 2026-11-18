import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminReviewService } from '@/services/admin/adminReview.service'

interface SetReviewHiddenVariables {
  id: number
  hidden: boolean
}

export const useAdminSetReviewHidden = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, hidden }: SetReviewHiddenVariables) =>
      hidden
        ? adminReviewService.hideReview(id)
        : adminReviewService.showReview(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'reviews'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
