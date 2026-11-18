import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'

interface EmptyStateProps {
  message: string
  accent: 'pink' | 'cyan'
  icon?: ReactNode
}

const ACCENTS = {
  pink: { icon: 'border-neon-pink/40 text-neon-pink', button: 'bg-neon-pink' },
  cyan: { icon: 'border-neon-cyan/40 text-neon-cyan', button: 'bg-neon-cyan' }
}

export const EmptyState = ({ message, accent, icon }: EmptyStateProps) => (
  <div className="mt-20 flex flex-col items-center gap-6 border border-dashed border-border py-20 text-center">
    {icon && (
      <div aria-hidden="true" className={`flex h-16 w-16 items-center justify-center border ${ACCENTS[accent].icon}`}>
        {icon}
      </div>
    )}
    <p className="max-w-md text-muted-foreground">{message}</p>
    <Link
      to="/shop"
      className={`${ACCENTS[accent].button} px-10 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80`}
    >
      Enter Archive
    </Link>
  </div>
)
