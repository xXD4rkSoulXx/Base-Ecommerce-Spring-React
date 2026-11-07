import type { OrderStatus } from '@/types/order'

export interface AdminCustomerSearchRequest {
  q?: string
  page?: number
  size?: number
}

export interface AdminCustomerOrderSearchRequest {
  page?: number
  size?: number
}

export interface AdminCustomerResponse {
  id: number
  email: string
  firstName: string
  lastName: string
  createdAt: string
  orders: number
  lastOrderAt?: string
}

export interface AdminCustomerOrderResponse {
  id: number
  status: OrderStatus
  createdAt: string
  totalAmount: number
  currency: string
}
