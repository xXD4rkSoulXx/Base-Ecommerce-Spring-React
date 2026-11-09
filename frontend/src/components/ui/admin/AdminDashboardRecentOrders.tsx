import { Link } from 'react-router-dom'
import type { AdminDashboardRecentOrderResponse } from '@/types/admin/adminDashboard'

interface AdminDashboardRecentOrdersProps {
  orders: AdminDashboardRecentOrderResponse[]
}

const currencyFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'short', timeStyle: 'short' })

export const AdminDashboardRecentOrders = ({ orders }: AdminDashboardRecentOrdersProps) => (
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
          <p className="font-mono text-[10px] text-muted-foreground">
            {dateFormatter.format(new Date(order.createdAt))}
          </p>
        </div>
        <div className="text-right">
          <p className="font-mono text-sm">{currencyFormatter.format(order.totalAmount)}</p>
          <p className="font-mono text-[10px] uppercase tracking-widest text-neon-cyan">
            {order.status.replaceAll('_', ' ')}
          </p>
        </div>
      </li>
    ))}
  </ul>
)
