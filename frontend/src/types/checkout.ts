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

export interface ShippingRateResponse {
  rateId: string
  carrier: string
  service: string
  amount: number
  currency: string
  estimatedDays: string
}

export interface CreateOrderRequest {
  address: CheckoutAddressRequest
  rateId: string
}

export interface CheckoutSessionResponse {
  url: string
}
