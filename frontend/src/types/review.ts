import type { Schema } from '@/types/schema'

export type ReviewEligibility = Schema<'ReviewEligibilityResponse'>['status']

export interface ReviewRequest {
  rating: number
  title: string
  description: string
}

export type ReviewResponse = Schema<'ReviewResponse'>

export type ReviewEligibilityResponse = Schema<'ReviewEligibilityResponse'>

export type ReviewLockReason = 'GUEST' | Exclude<ReviewEligibility, 'ELIGIBLE'>
