import { DetailLine } from '@/components/ui/DetailLine'
import { formatPrice } from '@/utils/format'
import type { OrderDetailsResponse } from '@/types/order'

interface OrderTotalsSectionProps {
  order: OrderDetailsResponse
}

export const OrderTotalsSection = ({ order }: OrderTotalsSectionProps) => (
  <div className="border border-border bg-background/40 p-4 font-mono text-xs">
    <DetailLine label="Subtotal" value={formatPrice(order.subtotal)} />
    {order.discountAmount > 0 && (
      <DetailLine
        label={order.discountCode ? `Discount (${order.discountCode})` : 'Discount'}
        value={`-${formatPrice(order.discountAmount)}`}
      />
    )}
    <DetailLine label="Shipping" value={formatPrice(order.shippingCost)} />
    <DetailLine label="Tax" value={formatPrice(order.taxAmount)} />
    <div className="mt-2 flex justify-between border-t border-border pt-2">
      <span className="uppercase tracking-widest">Total</span>
      <span className="text-lg text-neon-cyan">{formatPrice(order.totalAmount)}</span>
    </div>
  </div>
)
