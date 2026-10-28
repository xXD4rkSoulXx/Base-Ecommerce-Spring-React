const focusBorderByAccent = {
  cyan: 'focus:border-neon-cyan',
  pink: 'focus:border-neon-pink'
}

interface FormFieldProps {
  label: string
  value: string
  onChange: (value: string) => void
  accent: keyof typeof focusBorderByAccent
  type?: 'text' | 'password'
  autoComplete?: string
  disabled?: boolean
}

export const FormField = ({
  label,
  value,
  onChange,
  accent,
  type = 'text',
  autoComplete,
  disabled = false
}: FormFieldProps) => (
  <label className="block">
    <span className="mb-2 block font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
      {label}
    </span>
    <input
      type={type}
      value={value}
      onChange={(event) => onChange(event.target.value)}
      autoComplete={autoComplete}
      disabled={disabled}
      className={`w-full border border-border bg-background px-4 py-3 text-sm focus:outline-none disabled:opacity-50 ${focusBorderByAccent[accent]}`}
    />
  </label>
)
