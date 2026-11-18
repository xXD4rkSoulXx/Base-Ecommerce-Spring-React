import type { ReturnReason, ReturnStatus, ShippingAddress } from '@/types/order'
import type { ProductColor, ProductSize } from '@/types/product'

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
  ref: string
  productName: string
  color: ProductColor
  size: ProductSize
  imagePath?: string
  quantity: number
}
