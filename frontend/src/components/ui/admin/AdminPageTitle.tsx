import type { ReactNode } from 'react'

interface AdminPageTitleProps {
  count?: number
  action?: ReactNode
  children: ReactNode
}

export const AdminPageTitle = ({ count, action, children }: AdminPageTitleProps) => (
  <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
    <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">
      {children}
      {count !== undefined && ` (${count})`}
    </h1>
    {action}
  </div>
)
