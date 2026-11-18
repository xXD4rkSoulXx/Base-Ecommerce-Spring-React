import type { ReactNode } from 'react'

interface AdminInfoCardProps {
  label: string
  valueClassName?: string
  children: ReactNode
}

export const AdminInfoCard = ({ label, valueClassName = '', children }: AdminInfoCardProps) => (
  <div className="border border-border bg-card p-4">
    <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">{label}</p>
    <p className={`mt-2 text-sm ${valueClassName}`}>{children}</p>
  </div>
)
