import type { OrderStatus } from '@/types/order'

const statusConfig: Record<OrderStatus, { label: string; color: string }> = {
  PAID: { label: 'Paid', color: 'text-neon-pink' },
  PROCESSING: { label: 'Processing', color: 'text-neon-pink' },
  SHIPPED: { label: 'Shipped', color: 'text-neon-cyan' },
  DELIVERED: { label: 'Delivered', color: 'text-muted-foreground' },
  RETURN_REQUESTED: { label: 'Return Requested', color: 'text-neon-pink' },
  REFUNDED: { label: 'Refunded', color: 'text-muted-foreground' }
}

export const OrderStatusBadge = ({ status }: { status: OrderStatus }) => {
  const { label, color } = statusConfig[status]

  return (
    <span className={`font-mono text-[10px] uppercase tracking-widest ${color}`}>
      {label}
    </span>
  )
}
