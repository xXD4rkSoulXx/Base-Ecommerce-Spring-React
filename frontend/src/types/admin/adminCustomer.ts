import type { Schema } from '@/types/schema'

export interface AdminCustomerSearchRequest {
  q?: string
  page: number
  size: number
}

export interface AdminCustomerOrderSearchRequest {
  page: number
  size: number
}

export type AdminCustomerResponse = Schema<'AdminCustomerResponse'>
