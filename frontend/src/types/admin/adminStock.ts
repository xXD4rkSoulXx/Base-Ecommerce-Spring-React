import type { Schema } from '@/types/schema'

export interface AdminStockSearchRequest {
  q?: string
  lowStock?: boolean
  page?: number
  size?: number
}

export interface AdminAdjustStockRequest {
  delta: number
}

export interface AdminSetStockRequest {
  stock: number
}

export type AdminStockItemResponse = Schema<'AdminStockItemResponse'>
