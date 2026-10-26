import type { ReactNode } from 'react'

interface InfoBoxProps {
  icon: ReactNode
  label: string
  children: ReactNode
}

export const InfoBox = ({ icon, label, children }: InfoBoxProps) => (
  <div className="border border-border bg-background/40 p-3">
    <div className="flex items-center gap-1.5 text-muted-foreground">
      {icon}
      <span className="font-mono text-[9px] uppercase tracking-widest">{label}</span>
    </div>
    <div className="mt-1.5">{children}</div>
  </div>
)
