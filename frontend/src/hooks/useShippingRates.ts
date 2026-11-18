import { useMutation } from '@tanstack/react-query'
import { checkoutService } from '@/services/checkout.service'
import type { CheckoutAddressRequest } from '@/types/checkout'

export const useShippingRates = () =>
  useMutation({
    mutationFn: (request: CheckoutAddressRequest) => checkoutService.getShippingRates(request)
  })
