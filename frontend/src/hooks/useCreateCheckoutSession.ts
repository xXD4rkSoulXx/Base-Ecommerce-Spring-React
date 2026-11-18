import { useMutation } from '@tanstack/react-query'
import { checkoutService } from '@/services/checkout.service'
import type { CreateOrderRequest } from '@/types/checkout'

export const useCreateCheckoutSession = () =>
  useMutation({
    mutationFn: (request: CreateOrderRequest) => checkoutService.createSession(request),
    onSuccess: ({ url }) => window.location.assign(url)
  })
