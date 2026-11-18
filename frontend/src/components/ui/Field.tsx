import type { ComponentProps, ReactNode } from 'react'

interface FieldProps extends ComponentProps<'input'> {
  label: string
  icon?: ReactNode
}

export const Field = ({ label, icon, ...rest }: FieldProps) => {
  return (
    <label className="block">
      <span className="block font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
        {label}
      </span>
      <span className="mt-2 flex items-center gap-3 border border-border bg-card px-4 py-3 transition-colors focus-within:border-neon-pink">
        {icon && (
          <span aria-hidden="true" className="text-muted-foreground">
            {icon}
          </span>
        )}
        <input
          className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
          {...rest}
        />
      </span>
    </label>
  )
}
