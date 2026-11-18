import { api } from '@/lib/api'
import type { ReviewEligibilityResponse, ReviewRequest, ReviewResponse } from '@/types/review'

export const reviewService = {
  createReview: async (id: number | string, request: ReviewRequest) => {
    const response = await api.post<ReviewResponse>(`/api/products/${id}/review`, request)

    return response.data
  },

  getReviewEligibility: async (id: number | string) => {
    const response = await api.get<ReviewEligibilityResponse>(`/api/products/${id}/review/eligibility`)

    return response.data
  }
}
