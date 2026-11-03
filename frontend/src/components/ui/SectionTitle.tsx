import type { ReactNode } from 'react'

interface SectionTitleProps {
  icon: ReactNode
  children: ReactNode
}

export const SectionTitle = ({ icon, children }: SectionTitleProps) => (
  <div className="mb-3 flex items-center gap-2">
    <span className="text-neon-cyan">{icon}</span>
    <h2 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
      {children}
    </h2>
  </div>
)
