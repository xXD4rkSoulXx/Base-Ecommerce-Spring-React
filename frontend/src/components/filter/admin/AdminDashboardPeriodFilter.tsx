import { DASHBOARD_PERIODS } from '@/constants'
import { useAdminDashboardDays } from '@/hooks/admin/useAdminDashboardDays'
import { useUrlFilters } from '@/hooks/useUrlFilters'

export const AdminDashboardPeriodFilter = () => {
  const { changeFilters } = useUrlFilters()
  const days = useAdminDashboardDays()

  return (
    <div className="inline-flex border border-border bg-card">
      {DASHBOARD_PERIODS.map((period) => (
        <button
          key={period}
          type="button"
          aria-pressed={period === days}
          onClick={() => changeFilters({ days: period.toString() })}
          className={`px-4 py-2 font-mono text-xs uppercase tracking-widest ${
            period === days ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:text-foreground'
          }`}
        >
          {period} days
        </button>
      ))}
    </div>
  )
}
