import { useMutation } from '@tanstack/react-query'
import { checkoutService } from '@/services/checkout.service'

export const useCheckout = () => {
  const shippingRatesMutation = useMutation({
    mutationFn: checkoutService.getShippingRates
  })

  const createSessionMutation = useMutation({
    mutationFn: checkoutService.createSession,
    onSuccess: ({ url }) => window.location.assign(url)
  })

  return {
    shippingRates: shippingRatesMutation.data ?? [],
    isLoadingShippingRates: shippingRatesMutation.isPending,
    shippingRatesError: shippingRatesMutation.error,
    getShippingRates: shippingRatesMutation.mutate,
    isCreatingSession: createSessionMutation.isPending || createSessionMutation.isSuccess,
    createSessionError: createSessionMutation.error,
    createSession: createSessionMutation.mutate
  }
}
