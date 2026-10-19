import { api } from '@/lib/api'
import type { CheckoutAddressRequest, ShippingRateResponse, CheckoutSessionResponse, CreateOrderRequest } from '@/types/checkout'

export const checkoutService = {
  getShippingRates: async (request: CheckoutAddressRequest) => {
    const response = await api.post<ShippingRateResponse[]>('/api/checkout/shipping-rates', request)

    return response.data
  },

  createSession: async (request: CreateOrderRequest) => {
    const response = await api.post<CheckoutSessionResponse>('/api/checkout/session', request)

    return response.data
  }
}
