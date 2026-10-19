interface RowProps {
  label: string
  value: string
}

export const Row = ({ label, value }: RowProps) => (
  <div className="flex justify-between">
    <span className="text-muted-foreground">{label}</span>
    <span className="font-mono">{value}</span>
  </div>
)
