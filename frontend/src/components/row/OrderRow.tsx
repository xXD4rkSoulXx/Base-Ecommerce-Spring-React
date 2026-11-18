import { OrderStatusBadge } from '@/components/badge/OrderStatusBadge'
import { formatDate, formatPrice } from '@/utils/format'
import type { OrderSummaryResponse } from '@/types/order'

interface OrderRowProps {
  order: OrderSummaryResponse
  onSelect: (orderId: number) => void
}

export const OrderRow = ({ order, onSelect }: OrderRowProps) => (
  <div className="grid grid-cols-2 items-center gap-x-4 gap-y-2 border-b border-border py-5 sm:grid-cols-[1fr_9rem_7rem_5rem]">
    <div>
      <p className="font-mono text-xs text-neon-cyan">#{order.id}</p>
      <p className="mt-1 text-sm">{formatDate(order.createdAt)}</p>
    </div>
    <OrderStatusBadge status={order.status} />
    <span className="font-mono text-lg text-neon-cyan sm:text-right">{formatPrice(order.totalAmount)}</span>
    <button
      type="button"
      onClick={() => onSelect(order.id)}
      className="text-right text-xs font-bold uppercase tracking-widest hover:text-neon-cyan"
    >
      Detail →
    </button>
  </div>
)
