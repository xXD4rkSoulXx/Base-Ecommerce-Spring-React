import { useId, type ComponentProps, type ReactNode } from 'react'
import { FieldError } from '@/components/ui/FieldError'

const FOCUS_BORDER = {
  pink: 'focus-within:border-neon-pink',
  cyan: 'focus-within:border-neon-cyan'
}

interface FieldProps extends ComponentProps<'input'> {
  label: string
  icon?: ReactNode
  accent?: keyof typeof FOCUS_BORDER
  error?: string
}

export const Field = ({ label, icon, accent = 'pink', error, ...rest }: FieldProps) => {
  const errorId = useId()

  return (
    <div>
      <label className="block">
        <span className="block font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">{label}</span>
        <span
          className={`mt-2 flex items-center gap-3 border bg-card px-4 py-3 transition-colors in-[dialog]:bg-background ${
            error ? 'border-destructive' : 'border-border'
          } ${FOCUS_BORDER[accent]}`}
        >
          {icon && (
            <span aria-hidden="true" className="text-muted-foreground">
              {icon}
            </span>
          )}
          <input
            aria-invalid={!!error}
            aria-describedby={error ? errorId : undefined}
            className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none disabled:opacity-50"
            {...rest}
          />
        </span>
      </label>
      <FieldError id={errorId} message={error} />
    </div>
  )
}
