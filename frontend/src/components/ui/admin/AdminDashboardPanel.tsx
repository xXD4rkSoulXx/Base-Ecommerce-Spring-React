import type { ReactNode } from 'react'
import { FormError } from '@/components/ui/FormError'

interface AdminDashboardPanelProps {
  title: string
  isError: boolean
  isEmpty: boolean
  emptyMessage: string
  children: ReactNode
}

export const AdminDashboardPanel = ({
  title,
  isError,
  isEmpty,
  emptyMessage,
  children
}: AdminDashboardPanelProps) => (
  <div className="border border-border bg-card">
    <p className="border-b border-border p-4 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
      {title}
    </p>
    {isError ? (
      <div className="p-4">
        <FormError message="Could not load this block" />
      </div>
    ) : isEmpty ? (
      <p className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
        {emptyMessage}
      </p>
    ) : (
      children
    )}
  </div>
)
