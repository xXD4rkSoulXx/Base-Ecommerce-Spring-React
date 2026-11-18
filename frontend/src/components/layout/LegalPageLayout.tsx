import type { ReactNode } from 'react'
import { APP_NAME } from '@/constants'

interface LegalPageLayoutProps {
  title: string
  updated: string
  children: ReactNode
}

export const LegalPageLayout = ({ title, updated, children }: LegalPageLayoutProps) => (
  <div className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
    <div className="max-w-3xl">
      <p className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Last updated {updated}</p>
      <h1 className="mt-4 font-display text-5xl uppercase leading-[0.9] tracking-tighter md:text-7xl">{title}</h1>
      <div className="mt-8 border border-neon-pink/40 bg-neon-pink/5 p-4">
        <p className="font-mono text-[10px] uppercase tracking-[0.3em] text-neon-pink">Demo store</p>
        <p className="mt-2 text-sm text-muted-foreground">
          ${APP_NAME} is a portfolio project, not a real store. Payments run in Stripe test mode, shipping labels and tracking are simulated, and no order will ever be shipped.
        </p>
      </div>
      <div className="mt-12 space-y-10">{children}</div>
    </div>
  </div>
)
