import type { ReactNode } from 'react'

interface SectionTitleProps {
  icon: ReactNode
  as?: 'h2' | 'h3'
  action?: ReactNode
  children: ReactNode
}

export const SectionTitle = ({ icon, as: Heading = 'h2', action, children }: SectionTitleProps) => (
  <div className="mb-3 flex items-center justify-between gap-2">
    <div className="flex items-center gap-2">
      <span className="text-neon-cyan">{icon}</span>
      <Heading className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">{children}</Heading>
    </div>
    {action}
  </div>
)
