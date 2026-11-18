interface DetailLineProps {
  label: string
  value: string
  valueClassName?: string
}

export const DetailLine = ({ label, value, valueClassName = '' }: DetailLineProps) => (
  <div className="flex justify-between">
    <span className="text-muted-foreground">{label}</span>
    <span className={`font-mono ${valueClassName}`}>{value}</span>
  </div>
)
