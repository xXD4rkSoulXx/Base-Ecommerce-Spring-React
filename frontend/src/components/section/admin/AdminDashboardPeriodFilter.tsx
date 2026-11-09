import { useSearchParams } from 'react-router-dom'
import { useAdminDashboardDays } from '@/hooks/admin/useAdminDashboardDays'

export const AdminDashboardPeriodFilter = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const days = useAdminDashboardDays()

  const changePeriod = (value: number) => {
    const next = new URLSearchParams(searchParams)

    next.set('days', value.toString())
    setSearchParams(next)
  }

  return (
    <div className="inline-flex border border-border bg-card">
      {[7, 30, 90].map((period) => (
        <button
          key={period}
          onClick={() => changePeriod(period)}
          className={`px-4 py-2 font-mono text-xs uppercase tracking-widest ${
            period === days
              ? 'bg-primary text-primary-foreground'
              : 'text-muted-foreground hover:text-foreground'
          }`}
        >
          {period} days
        </button>
      ))}
    </div>
  )
}
