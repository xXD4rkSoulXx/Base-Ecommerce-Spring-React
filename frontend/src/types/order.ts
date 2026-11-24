import type { Schema } from '@/types/schema'

export type OrderStatus = Schema<'OrderSummaryResponse'>['status']

export type ShipmentStatus = Schema<'OrderShipmentResponse'>['status']

export type ReturnReason = Schema<'CreateReturnRequest'>['reason']

export type ReturnStatus = Schema<'OrderReturnResponse'>['status']

export interface OrderSearchRequest {
  q?: string
  from?: string
  to?: string
  page?: number
  size?: number
}

export interface CreateReturnRequest {
  reason: ReturnReason
  comment?: string
  items: CreateReturnItemRequest[]
}

export interface CreateReturnItemRequest {
  orderItemId: number
  quantity: number
}

export type ShippingAddress = Schema<'AdminReturnAddressResponse'>

export type OrderSummaryResponse = Schema<'OrderSummaryResponse'>

export type OrderItemResponse = Schema<'OrderItemResponse'>

export type ShipmentTrackingEventResponse = Schema<'ShipmentTrackingEventResponse'>

export type OrderShipmentResponse = Schema<'OrderShipmentResponse'>

export type OrderShipmentItemResponse = Schema<'OrderShipmentItemResponse'>

export type OrderStatusEventResponse = Schema<'OrderStatusEventResponse'>

export type OrderDetailsResponse = Schema<'OrderDetailsResponse'>

export type OrderReturnResponse = Schema<'OrderReturnResponse'>

export type OrderReturnItemResponse = Schema<'OrderReturnItemResponse'>
