import { OrderStatusBadge } from '@/components/badge/OrderStatusBadge'
import { formatDateTime } from '@/utils/format'
import type { OrderStatusEventResponse } from '@/types/order'

interface OrderTimelineSectionProps {
  events: OrderStatusEventResponse[]
}

export const OrderTimelineSection = ({ events }: OrderTimelineSectionProps) => (
  <ol>
    {events.map((event, index) => (
      <li key={`${event.status}-${event.occurredAt}`} className="relative flex items-center gap-4 pb-4 last:pb-0">
        {index < events.length - 1 && (
          <span className="absolute -bottom-2 left-[5px] top-2 w-px -translate-x-1/2 bg-neon-cyan/40" />
        )}
        <span className="relative z-10 h-2.5 w-2.5 shrink-0 rounded-full bg-neon-cyan" />
        <div className="flex flex-1 items-center justify-between gap-3">
          <OrderStatusBadge status={event.status} />
          <span className="font-mono text-[10px] text-muted-foreground">{formatDateTime(event.occurredAt)}</span>
        </div>
      </li>
    ))}
  </ol>
)
