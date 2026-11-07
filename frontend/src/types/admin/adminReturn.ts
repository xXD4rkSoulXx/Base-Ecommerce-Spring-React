import type { ReturnReason, ReturnStatus, ShippingAddress } from '@/types/order'

export interface AdminReturnSearchRequest {
  status?: ReturnStatus
  pending?: boolean
  page?: number
  size?: number
}

export interface AdminRejectReturnRequest {
  reason: string
}

export interface AdminReturnResponse {
  id: number
  orderId: number
  customerEmail: string
  address: ShippingAddress
  status: ReturnStatus
  reason: ReturnReason
  comment?: string
  refundAmount?: number
  rejectionReason?: string
  createdAt: string
  refundedAt?: string
  items: AdminReturnItemResponse[]
}

export interface AdminReturnItemResponse {
  productName: string
  color?: string
  size?: string
  imagePath?: string
  quantity: number
}
