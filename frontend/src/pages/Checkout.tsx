import { useState, type SubmitEvent } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { CheckoutAddressSection } from '@/components/section/CheckoutAddressSection'
import { CheckoutBlockedSection } from '@/components/section/CheckoutBlockedSection'
import { CheckoutReviewSection } from '@/components/section/CheckoutReviewSection'
import { CheckoutShippingSection } from '@/components/section/CheckoutShippingSection'
import { CheckoutSummarySection } from '@/components/section/CheckoutSummarySection'
import { StepIndicator } from '@/components/ui/StepIndicator'
import { useCart } from '@/hooks/useCart'
import { useCreateCheckoutSession } from '@/hooks/useCreateCheckoutSession'
import { useShippingRates } from '@/hooks/useShippingRates'
import { checkoutAddressSchema, type CheckoutAddressForm } from '@/schemas/checkout'
import type { CheckoutAddressRequest } from '@/types/checkout'
import { getErrorMessage } from '@/utils/error'

type Step = 1 | 2 | 3

const STEPS = ['Address', 'Shipping', 'Review']

const EMPTY_ADDRESS: CheckoutAddressForm = {
  recipientName: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  postalCode: '',
  country: 'PT',
  phone: ''
}

const toRequest = (address: CheckoutAddressForm): CheckoutAddressRequest => ({
  ...address,
  addressLine2: address.addressLine2 || undefined,
  state: address.state || undefined
})

export const Checkout = () => {
  const { items } = useCart()
  const {
    mutate: getShippingRates,
    data: shippingRates = [],
    isPending: isLoadingShippingRates,
    error: shippingRatesError
  } = useShippingRates()
  const { mutate: createSession, isPending: isCreatingSession, error: createSessionError } = useCreateCheckoutSession()

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<CheckoutAddressForm>({
    resolver: zodResolver(checkoutAddressSchema),
    defaultValues: EMPTY_ADDRESS
  })

  const [step, setStep] = useState<Step>(1)
  const [address, setAddress] = useState<CheckoutAddressForm | null>(null)
  const [selectedRateId, setSelectedRateId] = useState('')

  const selectedRate = shippingRates.find((rate) => rate.rateId === selectedRateId)
  const hasUnavailableItems = items.some((item) => item.available === false)

  const submitAddress = (data: CheckoutAddressForm) =>
    getShippingRates(toRequest(data), {
      onSuccess: (rates) => {
        setAddress(data)
        setSelectedRateId(rates[0]?.rateId ?? '')
        setStep(2)
      }
    })

  const submitStep = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (step === 2) {
      setStep(3)

      return
    }

    if (address) {
      createSession({ address: toRequest(address), rateId: selectedRateId })
    }
  }

  if (items.length === 0) {
    return <CheckoutBlockedSection title="Cart empty" actionLabel="Enter Archive" to="/shop" />
  }

  if (hasUnavailableItems) {
    return (
      <CheckoutBlockedSection
        title="Items unavailable"
        description="Some items in your cart are no longer available. Remove them to continue."
        actionLabel="Back to cart"
        to="/cart"
      />
    )
  }

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
      <h1 className="mb-10 mt-4 font-display text-5xl uppercase tracking-tighter md:text-6xl">
        Initiate <span className="text-neon-cyan">Transit</span>
      </h1>
      <StepIndicator steps={STEPS} current={step} />
      <div className="mt-12 grid gap-12 lg:grid-cols-[1fr_400px]">
        <form onSubmit={step === 1 ? handleSubmit(submitAddress) : submitStep} noValidate className="space-y-6">
          {step === 1 && (
            <CheckoutAddressSection
              register={register}
              errors={errors}
              error={shippingRatesError ? getErrorMessage(shippingRatesError, 'Could not load shipping rates') : null}
            />
          )}
          {step === 2 && (
            <CheckoutShippingSection
              rates={shippingRates}
              selectedRateId={selectedRateId}
              onSelect={setSelectedRateId}
            />
          )}
          {step === 3 && selectedRate && address && (
            <CheckoutReviewSection
              address={address}
              rate={selectedRate}
              error={createSessionError ? getErrorMessage(createSessionError, 'Could not start the payment') : null}
            />
          )}
          <div className="flex gap-3 pt-4">
            {step > 1 && (
              <button
                type="button"
                onClick={() => setStep((step - 1) as Step)}
                disabled={isCreatingSession}
                className="border border-border px-8 py-4 text-xs font-bold uppercase tracking-widest hover:border-foreground disabled:opacity-40"
              >
                ← Back
              </button>
            )}
            <button
              type="submit"
              disabled={isLoadingShippingRates || isCreatingSession || (step === 2 && !selectedRateId)}
              className="flex-1 bg-neon-cyan px-8 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80 disabled:cursor-not-allowed disabled:opacity-40"
            >
              {step === 1 && (isLoadingShippingRates ? 'Finding rates...' : 'Continue →')}
              {step === 2 && 'Continue →'}
              {step === 3 && (isCreatingSession ? 'Redirecting...' : 'Pay with Stripe →')}
            </button>
          </div>
        </form>
        <CheckoutSummarySection shippingAmount={selectedRate?.amount} />
      </div>
    </div>
  )
}
