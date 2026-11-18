import type { ProductColor, ProductSize } from '@/types/product'

export type OrderStatus = 'PAID' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'RETURN_REQUESTED' | 'REFUNDED'

export type ShipmentStatus = 'PRE_TRANSIT' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'FAILURE'

export type ReturnReason = 'DOES_NOT_FIT' | 'DEFECTIVE' | 'NOT_AS_DESCRIBED' | 'CHANGED_MIND' | 'OTHER'

export type ReturnStatus = 'REQUESTED' | 'APPROVED' | 'RECEIVED' | 'REFUNDED' | 'REJECTED'

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

export interface OrderSummaryResponse {
  id: number
  status: OrderStatus
  createdAt: string
  totalAmount: number
  currency: string
}

export interface OrderItemResponse {
  id: number
  ref: string
  productName: string
  color: ProductColor
  size: ProductSize
  imagePath?: string
  quantity: number
  unitPrice: number
  lineTotal: number
  returnableQuantity: number
}

export interface ShipmentTrackingEventResponse {
  status: ShipmentStatus
  description?: string
  location?: string
  occurredAt: string
}

export interface OrderShipmentResponse {
  id: number
  trackingNumber?: string
  status: ShipmentStatus
  items: OrderShipmentItemResponse[]
  events: ShipmentTrackingEventResponse[]
}

export interface OrderShipmentItemResponse {
  orderItemId: number
  quantity: number
}

export interface OrderStatusEventResponse {
  status: OrderStatus
  occurredAt: string
}

export interface ShippingAddress {
  recipientName: string
  addressLine1: string
  addressLine2?: string
  city: string
  state?: string
  postalCode: string
  country: string
  phone: string
}

export interface OrderDetailsResponse extends ShippingAddress {
  id: number
  status: OrderStatus
  createdAt: string
  currency: string
  discountCode?: string
  subtotal: number
  discountAmount: number
  shippingCost: number
  taxAmount: number
  totalAmount: number
  shippingCarrier: string
  shippingService: string
  events: OrderStatusEventResponse[]
  shipments: OrderShipmentResponse[]
  returnableUntil?: string
  returns: OrderReturnResponse[]
  items: OrderItemResponse[]
}

export interface OrderReturnResponse {
  id: number
  status: ReturnStatus
  reason: ReturnReason
  comment?: string
  refundAmount?: number
  createdAt: string
  refundedAt?: string
  rejectionReason?: string
  items: OrderReturnItemResponse[]
}

export interface OrderReturnItemResponse {
  orderItemId: number
  quantity: number
}
