import { formatPrice } from '@/utils/format'
import type { ShippingRateResponse } from '@/types/checkout'

interface ShippingRateRowProps {
  rate: ShippingRateResponse
  selected: boolean
  onSelect: () => void
}

export const ShippingRateRow = ({ rate, selected, onSelect }: ShippingRateRowProps) => (
  <label
    className={`flex cursor-pointer items-center justify-between gap-4 border p-4 transition-colors ${
      selected ? 'border-neon-cyan bg-card' : 'border-border hover:border-foreground'
    }`}
  >
    <div className="flex items-center gap-4">
      <input
        type="radio"
        name="rate"
        value={rate.rateId}
        checked={selected}
        onChange={onSelect}
        className="accent-neon-cyan"
      />
      <div>
        <p className="text-sm font-bold uppercase">
          {rate.carrier} · {rate.service}
        </p>
        <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          Delivery: {rate.estimatedDays}
        </p>
      </div>
    </div>
    <span className="font-mono text-sm text-neon-cyan">{formatPrice(rate.amount)}</span>
  </label>
)
