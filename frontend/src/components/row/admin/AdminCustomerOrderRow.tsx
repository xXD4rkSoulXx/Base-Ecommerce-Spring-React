import { Link } from 'react-router-dom'
import { OrderStatusBadge } from '@/components/badge/OrderStatusBadge'
import { usePermission } from '@/hooks/usePermission'
import type { OrderSummaryResponse } from '@/types/order'
import { formatDate, formatPrice } from '@/utils/format'

interface AdminCustomerOrderRowProps {
  order: OrderSummaryResponse
}

export const AdminCustomerOrderRow = ({ order }: AdminCustomerOrderRowProps) => {
  const canViewOrders = usePermission('ORDERS_READ')

  return (
    <tr className="border-b border-border last:border-b-0">
      <td className="p-4 font-mono">
        {canViewOrders ? (
          <Link to={`/admin/orders/${order.id}`} className="hover:text-neon-cyan">
            #{order.id}
          </Link>
        ) : (
          <span>#{order.id}</span>
        )}
      </td>
      <td className="p-4 font-mono text-xs text-muted-foreground">{formatDate(order.createdAt)}</td>
      <td className="p-4">
        <OrderStatusBadge status={order.status} />
      </td>
      <td className="p-4 text-right font-mono text-neon-cyan">{formatPrice(order.totalAmount)}</td>
    </tr>
  )
}
