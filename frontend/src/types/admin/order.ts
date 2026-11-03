import type { OrderDetailsResponse, OrderStatus, ShipmentStatus } from '@/types/order'

export interface AdminOrderSearchRequest {
  q?: string
  status?: OrderStatus
  pending?: boolean
  from?: string
  to?: string
  page?: number
  size?: number
}

export interface UpdateOrderStatusRequest {
  status: OrderStatus
}

export interface UpdateShipmentStatusRequest {
  status: ShipmentStatus
}

export interface AdminOrderSummaryResponse {
  id: number
  status: OrderStatus
  createdAt: string
  totalAmount: number
  currency: string
  customerEmail: string
  recipientName: string
}

export interface AdminOrderDetailsResponse {
  customerEmail: string
  customerName: string
  nextStatus?: OrderStatus
  shipmentActions: Record<number, ShipmentStatus[]>
  order: OrderDetailsResponse
}
