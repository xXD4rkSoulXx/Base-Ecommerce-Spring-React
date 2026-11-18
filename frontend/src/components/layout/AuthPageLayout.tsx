import type { ReactNode } from 'react'

type AuthPageSide = 'left' | 'right'
type AuthPageTone = 'cyan' | 'pink'

interface AuthPageLayoutProps {
  side: AuthPageSide
  tone: AuthPageTone
  asideTitle: ReactNode
  asideDescription: string
  asideFooter?: ReactNode
  children: ReactNode
}

const GRADIENT_CLASS_NAMES: Record<AuthPageTone, string> = {
  cyan: 'bg-gradient-to-br from-transparent via-transparent to-neon-cyan/10',
  pink: 'bg-gradient-to-tl from-transparent via-transparent to-neon-pink/15'
}

export const AuthPageLayout = ({
  side,
  tone,
  asideTitle,
  asideDescription,
  asideFooter,
  children
}: AuthPageLayoutProps) => {
  const aside = (
    <aside className="relative hidden overflow-hidden border border-border bg-card lg:block">
      <div className="grid-noise absolute inset-0 opacity-40" />
      <div className={`absolute inset-0 ${GRADIENT_CLASS_NAMES[tone]}`} />
      <div className="relative flex h-full flex-col justify-between p-10">
        <div>
          <p className="mt-6 font-display text-6xl uppercase leading-[0.9] tracking-tighter">{asideTitle}</p>
          <p className="mt-6 max-w-sm text-sm text-muted-foreground">{asideDescription}</p>
        </div>
        {asideFooter && <div className="space-y-3 border-t border-border pt-6">{asideFooter}</div>}
      </div>
    </aside>
  )

  return (
    <div className="relative mx-auto grid min-h-[calc(100vh-80px)] max-w-[1440px] gap-0 px-6 py-16 md:px-10 lg:grid-cols-2 lg:gap-16">
      {side === 'left' && aside}
      <section className="flex flex-col justify-center">{children}</section>
      {side === 'right' && aside}
    </div>
  )
}
