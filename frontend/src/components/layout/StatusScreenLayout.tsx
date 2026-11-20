import type { ReactNode } from 'react'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'

type StatusScreenGlow = 'top' | 'bottom'

interface StatusScreenLayoutProps {
  title?: string
  glow?: StatusScreenGlow
  children: ReactNode
}

const GLOW_CLASS_NAMES: Record<StatusScreenGlow, string> = {
  top: '-top-32',
  bottom: '-bottom-32'
}

export const StatusScreenLayout = ({ title, glow, children }: StatusScreenLayoutProps) => {
  useDocumentTitle(title)

  return (
    <div className="relative flex min-h-dvh items-center justify-center overflow-hidden bg-background px-6 text-foreground">
      <div className="grid-noise pointer-events-none absolute inset-0" />
      {glow && (
        <div
          className={`pointer-events-none absolute left-1/2 h-[420px] w-[420px] -translate-x-1/2 rounded-full bg-neon-pink opacity-30 blur-3xl ${GLOW_CLASS_NAMES[glow]}`}
        />
      )}
      <div className="relative z-10 mx-auto max-w-2xl text-center">{children}</div>
    </div>
  )
}
