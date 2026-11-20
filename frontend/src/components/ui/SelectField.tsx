import { useId, type ComponentProps } from 'react'
import { FieldError } from '@/components/ui/FieldError'

interface SelectFieldProps extends ComponentProps<'select'> {
  label: string
  error?: string
}

export const SelectField = ({ label, error, children, ...rest }: SelectFieldProps) => {
  const errorId = useId()

  return (
    <div>
      <label className="block">
        <span className="block font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">{label}</span>
        <select
          aria-invalid={!!error}
          aria-describedby={error ? errorId : undefined}
          className={`mt-2 w-full border bg-card px-4 py-3 text-sm transition-colors in-[dialog]:bg-background focus:border-neon-cyan focus:outline-none disabled:opacity-50 ${
            error ? 'border-destructive' : 'border-border'
          }`}
          {...rest}
        >
          {children}
        </select>
      </label>
      <FieldError id={errorId} message={error} />
    </div>
  )
}
