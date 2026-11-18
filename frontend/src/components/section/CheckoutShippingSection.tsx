import { ShippingRateRow } from '@/components/row/ShippingRateRow'
import type { ShippingRateResponse } from '@/types/checkout'

interface CheckoutShippingSectionProps {
  rates: ShippingRateResponse[]
  selectedRateId: string
  onSelect: (rateId: string) => void
}

export const CheckoutShippingSection = ({ rates, selectedRateId, onSelect }: CheckoutShippingSectionProps) => (
  <div className="space-y-3">
    <p className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Shipping method</p>
    {rates.map((rate) => (
      <ShippingRateRow
        key={rate.rateId}
        rate={rate}
        selected={rate.rateId === selectedRateId}
        onSelect={() => onSelect(rate.rateId)}
      />
    ))}
  </div>
)
