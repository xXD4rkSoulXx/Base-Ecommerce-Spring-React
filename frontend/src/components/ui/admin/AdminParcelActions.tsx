import type { ShipmentStatus } from '@/types/order'

interface AdminParcelActionsProps {
  actions: ShipmentStatus[]
  disabled: boolean
  onChange: (status: ShipmentStatus) => void
}

const ACTION_LABELS: Partial<Record<ShipmentStatus, string>> = {
  IN_TRANSIT: 'Shipped',
  OUT_FOR_DELIVERY: 'Out for delivery',
  DELIVERED: 'Delivered',
  FAILURE: 'Delivery failed'
}

export const AdminParcelActions = ({ actions, disabled, onChange }: AdminParcelActionsProps) => (
  <>
    {actions.map((action) => (
      <button
        key={action}
        onClick={() => onChange(action)}
        disabled={disabled}
        className={`border px-3 py-1.5 font-mono text-[10px] uppercase tracking-widest disabled:opacity-50 ${
          action === 'FAILURE'
            ? 'border-neon-pink/50 text-neon-pink hover:bg-neon-pink/10'
            : 'border-neon-cyan/50 text-neon-cyan hover:bg-neon-cyan/10'
        }`}
      >
        {ACTION_LABELS[action]}
      </button>
    ))}
  </>
)
