import { Link } from 'react-router-dom'
import { OrderStatusBadge } from '@/components/badge/OrderStatusBadge'
import { formatCompactDateTime, formatPrice } from '@/utils/format'
import type { AdminDashboardRecentOrderResponse } from '@/types/admin/adminDashboard'

interface AdminDashboardRecentOrdersSectionProps {
  orders: AdminDashboardRecentOrderResponse[]
}

export const AdminDashboardRecentOrdersSection = ({ orders }: AdminDashboardRecentOrdersSectionProps) => (
  <ul>
    {orders.map((order) => (
      <li
        key={order.id}
        className="flex items-center justify-between gap-4 border-b border-border p-4 last:border-b-0"
      >
        <div className="min-w-0">
          <Link to={`/admin/orders/${order.id}`} className="font-bold hover:text-neon-cyan">
            #{order.id}
          </Link>
          <p className="truncate text-xs text-muted-foreground">{order.customerName}</p>
          <p className="font-mono text-[10px] text-muted-foreground">{formatCompactDateTime(order.createdAt)}</p>
        </div>
        <div className="text-right">
          <p className="font-mono text-sm">{formatPrice(order.totalAmount)}</p>
          <OrderStatusBadge status={order.status} />
        </div>
      </li>
    ))}
  </ul>
)
