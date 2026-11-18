import { Check } from 'lucide-react'

interface StepIndicatorProps {
  steps: string[]
  current: number
}

export const StepIndicator = ({ steps, current }: StepIndicatorProps) => (
  <ol className="flex items-center gap-4 border-b border-border pb-6">
    {steps.map((label, index) => {
      const number = index + 1
      const active = current === number
      const done = current > number

      return (
        <li key={label} aria-current={active ? 'step' : undefined} className="flex items-center gap-3">
          <div
            className={`flex h-8 w-8 items-center justify-center border font-mono text-xs font-bold ${
              active
                ? 'border-neon-cyan bg-neon-cyan text-primary-foreground'
                : done
                  ? 'border-neon-cyan text-neon-cyan'
                  : 'border-border text-muted-foreground'
            }`}
          >
            {done ? <Check className="h-4 w-4" /> : number}
          </div>
          <span
            className={`text-xs font-bold uppercase tracking-widest ${
              active ? 'text-foreground' : 'text-muted-foreground'
            }`}
          >
            {label}
          </span>
          {index < steps.length - 1 && <span className="ml-2 h-px w-8 bg-border" />}
        </li>
      )
    })}
  </ol>
)
