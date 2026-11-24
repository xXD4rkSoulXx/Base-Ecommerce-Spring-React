import type { Schema } from '@/types/schema'

export interface CreateOrderRequest {
  address: CheckoutAddressRequest
  rateId: string
}

export interface CheckoutAddressRequest {
  recipientName: string
  addressLine1: string
  addressLine2?: string
  city: string
  state?: string
  postalCode: string
  country: string
  phone: string
}

export type ShippingRateResponse = Schema<'ShippingRateResponse'>

export type CheckoutSessionResponse = Schema<'CheckoutSessionResponse'>
