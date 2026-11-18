export type ReviewEligibility = 'ELIGIBLE' | 'NOT_PURCHASED' | 'ALREADY_REVIEWED'

export interface ReviewRequest {
  rating: number
  title: string
  description: string
}

export interface ReviewResponse {
  id: number
  title: string
  description: string
  rating: number
  userFullName: string
  createdAt: string
}

export interface ReviewEligibilityResponse {
  status: ReviewEligibility
}

export type ReviewLockReason = 'GUEST' | Exclude<ReviewEligibility, 'ELIGIBLE'>
