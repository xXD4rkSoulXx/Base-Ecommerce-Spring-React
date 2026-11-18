import { ORDER_STATUS_LABELS } from '@/constants'
import type { OrderStatus } from '@/types/order'

interface OrderStatusBadgeProps {
  status: OrderStatus
}

const ORDER_STATUS_COLORS: Record<OrderStatus, string> = {
  PAID: 'text-neon-pink',
  PROCESSING: 'text-neon-pink',
  SHIPPED: 'text-neon-cyan',
  DELIVERED: 'text-muted-foreground',
  RETURN_REQUESTED: 'text-neon-pink',
  REFUNDED: 'text-muted-foreground'
}

export const OrderStatusBadge = ({ status }: OrderStatusBadgeProps) => (
  <span className={`font-mono text-[10px] uppercase tracking-widest ${ORDER_STATUS_COLORS[status]}`}>
    {ORDER_STATUS_LABELS[status]}
  </span>
)
