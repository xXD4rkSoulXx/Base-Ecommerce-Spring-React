import type { ReactNode } from 'react'

type StatusBadgeTone = 'cyan' | 'pink' | 'muted'

interface StatusBadgeProps {
  tone: StatusBadgeTone
  children: ReactNode
}

const TONE_CLASS_NAMES: Record<StatusBadgeTone, string> = {
  cyan: 'border-neon-cyan text-neon-cyan',
  pink: 'border-neon-pink text-neon-pink',
  muted: 'border-border text-muted-foreground'
}

export const StatusBadge = ({ tone, children }: StatusBadgeProps) => (
  <span
    className={`inline-block border px-2 py-0.5 font-mono text-[10px] uppercase tracking-widest ${TONE_CLASS_NAMES[tone]}`}
  >
    {children}
  </span>
)
