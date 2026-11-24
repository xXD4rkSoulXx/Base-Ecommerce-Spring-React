import type { Schema } from '@/types/schema'

export interface AdminReviewSearchRequest {
  q?: string
  hidden?: boolean
  page: number
  size: number
}

export type AdminReviewResponse = Schema<'AdminReviewResponse'>
