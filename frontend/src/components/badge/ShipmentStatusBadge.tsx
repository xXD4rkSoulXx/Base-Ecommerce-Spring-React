import { SHIPMENT_STATUS_LABELS } from '@/constants'
import type { ShipmentStatus } from '@/types/order'

interface ShipmentStatusBadgeProps {
  status: ShipmentStatus
}

const SHIPMENT_STATUS_COLORS: Record<ShipmentStatus, string> = {
  PRE_TRANSIT: 'text-neon-pink',
  IN_TRANSIT: 'text-neon-cyan',
  OUT_FOR_DELIVERY: 'text-neon-cyan',
  DELIVERED: 'text-muted-foreground',
  FAILURE: 'text-neon-pink'
}

export const ShipmentStatusBadge = ({ status }: ShipmentStatusBadgeProps) => (
  <span className={`font-mono text-[10px] uppercase tracking-widest ${SHIPMENT_STATUS_COLORS[status]}`}>
    {SHIPMENT_STATUS_LABELS[status]}
  </span>
)
