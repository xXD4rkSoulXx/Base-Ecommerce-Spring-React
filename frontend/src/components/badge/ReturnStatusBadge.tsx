import { RETURN_STATUS_LABELS } from '@/constants'
import type { ReturnStatus } from '@/types/order'

interface ReturnStatusBadgeProps {
  status: ReturnStatus
}

const RETURN_STATUS_COLORS: Record<ReturnStatus, string> = {
  REQUESTED: 'text-neon-pink',
  APPROVED: 'text-neon-cyan',
  RECEIVED: 'text-neon-cyan',
  REFUNDED: 'text-muted-foreground',
  REJECTED: 'text-destructive'
}

export const ReturnStatusBadge = ({ status }: ReturnStatusBadgeProps) => (
  <span className={`font-mono text-[10px] uppercase tracking-widest ${RETURN_STATUS_COLORS[status]}`}>
    {RETURN_STATUS_LABELS[status]}
  </span>
)
