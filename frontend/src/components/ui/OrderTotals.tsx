import { SummaryLine } from '@/components/ui/SummaryLine'
import type { OrderDetailsResponse } from '@/types/order'

interface OrderTotalsProps {
  order: OrderDetailsResponse
}

const priceFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const formatPrice = (amount: number) => priceFormatter.format(amount)

export const OrderTotals = ({ order }: OrderTotalsProps) => (
  <div className="border border-border bg-background/40 p-4 font-mono text-xs">
    <SummaryLine label="Subtotal" value={formatPrice(order.subtotal)} />
    {order.discountAmount > 0 && (
      <SummaryLine
        label={order.discountCode ? `Discount (${order.discountCode})` : 'Discount'}
        value={`-${formatPrice(order.discountAmount)}`}
      />
    )}
    <SummaryLine label="Shipping" value={formatPrice(order.shippingCost)} />
    <SummaryLine label="Tax" value={formatPrice(order.taxAmount)} />
    <div className="mt-2 flex justify-between border-t border-border pt-2">
      <span className="uppercase tracking-widest">Total</span>
      <span className="text-lg text-neon-cyan">{formatPrice(order.totalAmount)}</span>
    </div>
  </div>
)
