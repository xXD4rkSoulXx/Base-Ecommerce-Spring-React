import { Link } from 'react-router-dom'
import { OrderStatusBadge } from '@/components/ui/OrderStatusBadge'
import type { AdminOrderSummaryResponse } from '@/types/admin/adminOrder'

interface AdminOrderRowProps {
  order: AdminOrderSummaryResponse
}

const priceFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const dateTimeFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium', timeStyle: 'short' })

export const AdminOrderRow = ({ order }: AdminOrderRowProps) => (
  <tr className="border-b border-border last:border-b-0">
    <td className="p-4 font-mono text-xs text-neon-cyan">#{order.id}</td>
    <td className="p-4">
      <p className="font-bold">{order.recipientName}</p>
      <p className="text-xs text-muted-foreground">{order.customerEmail}</p>
    </td>
    <td className="p-4 font-mono text-xs text-muted-foreground">
      {dateTimeFormatter.format(new Date(order.createdAt))}
    </td>
    <td className="p-4">
      <OrderStatusBadge status={order.status} />
    </td>
    <td className="p-4 text-right font-mono text-neon-cyan">
      {priceFormatter.format(order.totalAmount)}
    </td>
    <td className="p-4 text-right">
      <Link
        to={`/admin/orders/${order.id}`}
        className="text-xs font-bold uppercase tracking-widest hover:text-neon-cyan"
      >
        Detail →
      </Link>
    </td>
  </tr>
)
