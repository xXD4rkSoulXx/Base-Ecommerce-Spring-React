import type { ReturnStatus } from '@/types/order'

const statusConfig: Record<ReturnStatus, { label: string; color: string }> = {
  REQUESTED: { label: 'Requested', color: 'text-neon-pink' },
  APPROVED: { label: 'Approved: send the items back', color: 'text-neon-cyan' },
  RECEIVED: { label: 'Received', color: 'text-neon-cyan' },
  REFUNDED: { label: 'Refunded', color: 'text-muted-foreground' },
  REJECTED: { label: 'Rejected', color: 'text-red-500' }
}

export const ReturnStatusBadge = ({ status }: { status: ReturnStatus }) => {
  const { label, color } = statusConfig[status]

  return (
    <span className={`font-mono text-[10px] uppercase tracking-widest ${color}`}>
      {label}
    </span>
  )
}
