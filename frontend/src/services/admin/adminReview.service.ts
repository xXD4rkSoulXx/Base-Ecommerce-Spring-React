import { api } from '@/lib/api'
import type { AdminReviewResponse, AdminReviewSearchRequest } from '@/types/admin/adminReview'
import type { PageResponse } from '@/types/page'

export const adminReviewService = {
  hideReview: async (id: number) => {
    await api.post(`/api/admin/reviews/${id}/hide`)
  },

  showReview: async (id: number) => {
    await api.post(`/api/admin/reviews/${id}/show`)
  },

  getReviews: async (request: AdminReviewSearchRequest) => {
    const response = await api.get<PageResponse<AdminReviewResponse>>("/api/admin/reviews", {
      params: request
    })

    return response.data
  },
}
