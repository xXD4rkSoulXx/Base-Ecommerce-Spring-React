interface AdminDashboardKpiProps {
  label: string
  value: number | undefined
  previous: number | undefined
  format: (value: number) => string
  higherIsBetter?: boolean
}

const percentFormatter = new Intl.NumberFormat('pt-PT', {
  maximumFractionDigits: 1,
  signDisplay: 'exceptZero'
})

export const AdminDashboardKpi = ({
  label,
  value,
  previous,
  format,
  higherIsBetter = true
}: AdminDashboardKpiProps) => {
  const delta =
    value !== undefined && previous !== undefined && previous !== 0
      ? ((value - previous) / Math.abs(previous)) * 100
      : null
  const isGood = delta !== null && (delta >= 0) === higherIsBetter

  return (
    <div className="border border-border bg-card p-5">
      <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">{label}</p>
      <p className="mt-2 font-display text-3xl tracking-tighter">
        {value === undefined ? '—' : format(value)}
      </p>
      <p
        className={`mt-1 font-mono text-[10px] uppercase tracking-widest ${
          delta === null ? 'text-muted-foreground' : isGood ? 'text-neon-cyan' : 'text-neon-pink'
        }`}
      >
        {delta === null ? 'No previous data' : `${percentFormatter.format(delta)}% vs previous period`}
      </p>
    </div>
  )
}
