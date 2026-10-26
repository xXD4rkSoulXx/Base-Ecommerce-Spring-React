interface SummaryLineProps {
  label: string
  value: string
}

export const SummaryLine = ({ label, value }: SummaryLineProps) => (
  <div className="flex justify-between py-0.5">
    <span className="text-muted-foreground uppercase tracking-widest">{label}</span>
    <span>{value}</span>
  </div>
)
