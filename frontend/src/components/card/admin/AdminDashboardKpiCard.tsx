import { formatPercentChange } from '@/utils/format'

interface AdminDashboardKpiCardProps {
  label: string
  value: number | undefined
  previous: number | undefined
  format: (value: number) => string
  higherIsBetter?: boolean
}

export const AdminDashboardKpiCard = ({ label, value, previous, format, higherIsBetter = true }: AdminDashboardKpiCardProps) => {
  const delta =
    value !== undefined && previous !== undefined && previous !== 0
      ? ((value - previous) / Math.abs(previous)) * 100
      : null

  const deltaColor =
    delta === null || delta === 0
      ? 'text-muted-foreground'
      : (delta > 0) === higherIsBetter
        ? 'text-neon-cyan'
        : 'text-neon-pink'

  return (
    <div className="border border-border bg-card p-5">
      <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">{label}</p>
      <p className="mt-2 font-display text-3xl tracking-tighter">
        {value === undefined ? '—' : format(value)}
      </p>
      <p className={`mt-1 font-mono text-[10px] uppercase tracking-widest ${deltaColor}`}>
        {delta === null ? 'No previous data' : `${formatPercentChange(delta)}% vs previous period`}
      </p>
    </div>
  )
}
