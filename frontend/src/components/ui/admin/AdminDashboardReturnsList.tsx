import type { AdminDashboardReturnResponse } from '@/types/admin/adminDashboard'

interface AdminDashboardReturnsListProps {
  returns: AdminDashboardReturnResponse[]
}

const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'short', timeStyle: 'short' })

export const AdminDashboardReturnsList = ({ returns }: AdminDashboardReturnsListProps) => (
  <ul>
    {returns.map((orderReturn) => (
      <li
        key={orderReturn.id}
        className="flex items-center justify-between gap-4 border-b border-border p-4 last:border-b-0"
      >
        <div className="min-w-0">
          <p className="font-bold">
            Return #{orderReturn.id}{' '}
            <span className="font-mono text-xs text-muted-foreground">· order #{orderReturn.orderId}</span>
          </p>
          <p className="truncate text-xs text-muted-foreground">{orderReturn.customerName}</p>
          <p className="font-mono text-[10px] text-muted-foreground">
            {dateFormatter.format(new Date(orderReturn.createdAt))}
          </p>
        </div>
        <p className="font-mono text-[10px] uppercase tracking-widest text-neon-pink">
          {orderReturn.status.replaceAll('_', ' ')}
        </p>
      </li>
    ))}
  </ul>
)
