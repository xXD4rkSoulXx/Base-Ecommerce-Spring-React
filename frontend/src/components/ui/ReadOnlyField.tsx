interface ReadOnlyFieldProps {
  label: string
  value: string
}

export const ReadOnlyField = ({ label, value }: ReadOnlyFieldProps) => (
  <div>
    <p className="block font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">{label}</p>
    <p className="mt-2 border border-border bg-background px-4 py-3 font-mono text-sm">{value}</p>
  </div>
)
