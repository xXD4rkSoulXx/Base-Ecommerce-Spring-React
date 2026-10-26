import type { ShipmentStatus } from '@/types/order'

const statusConfig: Record<ShipmentStatus, { label: string; color: string }> = {
  PRE_TRANSIT: { label: 'Pre-transit', color: 'text-neon-pink' },
  IN_TRANSIT: { label: 'In transit', color: 'text-neon-cyan' },
  OUT_FOR_DELIVERY: { label: 'Out for delivery', color: 'text-neon-cyan' },
  DELIVERED: { label: 'Delivered', color: 'text-muted-foreground' },
  FAILURE: { label: 'Failure', color: 'text-neon-pink' },
  RETURNED: { label: 'Returned', color: 'text-muted-foreground' }
}

export const ShipmentStatusBadge = ({ status }: { status: ShipmentStatus }) => {
  const { label, color } = statusConfig[status]

  return (
    <span className={`font-mono text-[10px] uppercase tracking-widest ${color}`}>
      {label}
    </span>
  )
}
