import type { InputHTMLAttributes, ReactNode, Ref } from 'react'

interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  icon: ReactNode
  ref?: Ref<HTMLInputElement>
}

export const Field = ({ label, icon, ...rest }: FieldProps) => {
  return (
    <div>
      <label className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
        {label}
      </label>
      <div className="mt-2 flex items-center gap-3 border border-border bg-card px-4 py-3 transition-colors focus-within:border-neon-pink">
        <span className="text-muted-foreground">{icon}</span>
        <input
          className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
          {...rest}
        />
      </div>
    </div>
  )
}
