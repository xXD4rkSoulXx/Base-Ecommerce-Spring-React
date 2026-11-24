import type { OrderStatus, ShipmentStatus } from '@/types/order'
import type { Schema } from '@/types/schema'

export interface AdminOrderSearchRequest {
  q?: string
  status?: OrderStatus
  pending?: boolean
  from?: string
  to?: string
  page?: number
  size?: number
}

export interface AdminUpdateOrderStatusRequest {
  status: OrderStatus
}

export interface AdminUpdateShipmentStatusRequest {
  status: ShipmentStatus
}

export type AdminOrderSummaryResponse = Schema<'AdminOrderSummaryResponse'>

export type AdminOrderDetailsResponse = Schema<'AdminOrderDetailsResponse'>
