import { Link } from 'react-router-dom'
import type { AdminDashboardLowStockResponse } from '@/types/admin/adminDashboard'

interface AdminDashboardLowStockListProps {
  items: AdminDashboardLowStockResponse[]
}

export const AdminDashboardLowStockList = ({ items }: AdminDashboardLowStockListProps) => (
  <ul>
    {items.map((item) => (
      <li
        key={item.skuId}
        className="flex items-center justify-between gap-4 border-b border-border p-4 last:border-b-0"
      >
        <div className="min-w-0">
          <Link
            to={`/admin/stock?q=${encodeURIComponent(item.ref)}`}
            className="block truncate font-bold hover:text-neon-cyan"
          >
            {item.productName}
          </Link>
          <p className="font-mono text-xs text-muted-foreground">
            {item.ref} · {item.size} {item.color}
          </p>
        </div>
        <p className={`font-mono text-sm ${item.stock === 0 ? 'text-neon-pink' : 'text-neon-yellow'}`}>
          {item.stock}
        </p>
      </li>
    ))}
  </ul>
)
