import { FormError } from '@/components/ui/FormError'
import type { CheckoutAddressForm, ShippingRateResponse } from '@/types/checkout'
import { formatCountry, formatPrice } from '@/utils/format'

interface CheckoutReviewSectionProps {
  address: CheckoutAddressForm
  rate: ShippingRateResponse
  error: string | null
}

export const CheckoutReviewSection = ({ address, rate, error }: CheckoutReviewSectionProps) => (
  <div className="space-y-4">
    <div className="border border-border bg-card p-4">
      <p className="font-mono text-[10px] uppercase tracking-widest text-neon-cyan">Ship to</p>
      <p className="mt-2 text-sm font-bold">{address.recipientName}</p>
      <p className="text-sm text-muted-foreground">
        {address.addressLine1}
        {address.addressLine2 && `, ${address.addressLine2}`}
      </p>
      <p className="text-sm text-muted-foreground">
        {address.postalCode} {address.city}
        {address.state && `, ${address.state}`}
        {' · '}
        {formatCountry(address.country)}
      </p>
      <p className="text-sm text-muted-foreground">{address.phone}</p>
    </div>
    <div className="border border-border bg-card p-4">
      <p className="font-mono text-[10px] uppercase tracking-widest text-neon-cyan">Shipping method</p>
      <p className="mt-2 text-sm">
        {rate.carrier} · {rate.service} · {formatPrice(rate.amount)}
      </p>
    </div>
    <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
      You will complete the payment securely on Stripe.
    </p>
    {error && <FormError message={error} />}
  </div>
)
