import type { ReturnStatus } from '@/types/order'
import type { Schema } from '@/types/schema'

export interface AdminReturnSearchRequest {
  status?: ReturnStatus
  pending?: boolean
  page?: number
  size?: number
}

export interface AdminRejectReturnRequest {
  reason: string
}

export type AdminReturnResponse = Schema<'AdminReturnResponse'>

export type AdminReturnItemResponse = Schema<'AdminReturnItemResponse'>
