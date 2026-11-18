import { useState, type ReactNode } from 'react'
import { ChevronDown } from 'lucide-react'
import { ShipmentStatusBadge } from '@/components/badge/ShipmentStatusBadge'
import { formatDateTime, formatSize } from '@/utils/format'
import type { OrderItemResponse, OrderShipmentResponse } from '@/types/order'

interface ParcelRowProps {
  shipment: OrderShipmentResponse
  index: number
  orderItems: OrderItemResponse[]
  actions?: ReactNode
}

export const ParcelRow = ({ shipment, index, orderItems, actions }: ParcelRowProps) => {
  const [isOpen, setIsOpen] = useState(false)

  const toggle = () => setIsOpen((current) => !current)

  return (
    <div className="border-b border-border last:border-b-0">
      <div className="flex flex-wrap items-center">
        <button
          type="button"
          onClick={toggle}
          aria-expanded={isOpen}
          className="min-w-0 flex-1 py-2 pl-3 pr-2 text-left hover:bg-background/40"
        >
          <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Box {index + 1}</span>
        </button>
        {actions && <div className="flex gap-2 py-2">{actions}</div>}
        <button
          type="button"
          onClick={toggle}
          aria-expanded={isOpen}
          className="flex shrink-0 items-center gap-3 py-2 pl-3 pr-3 hover:bg-background/40"
        >
          <ShipmentStatusBadge status={shipment.status} />
          <span className="font-mono text-xs text-neon-cyan">{shipment.trackingNumber ?? '—'}</span>
          <ChevronDown
            className={`h-3.5 w-3.5 text-muted-foreground transition-transform ${isOpen ? 'rotate-180' : ''}`}
          />
        </button>
      </div>
      {isOpen && (
        <div className="space-y-4 border-t border-border bg-background/40 px-3 py-3">
          {shipment.items.length > 0 && (
            <div>
              <p className="mb-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Contents</p>
              <ul className="space-y-2">
                {shipment.items.map((item) => {
                  const orderItem = orderItems.find((candidate) => candidate.id === item.orderItemId)

                  if (!orderItem) {
                    return null
                  }

                  return (
                    <li key={item.orderItemId} className="flex items-center gap-3 text-xs">
                      <div className="h-10 w-10 shrink-0 overflow-hidden border border-border bg-background">
                        {orderItem.imagePath && (
                          <img src={orderItem.imagePath} alt="" loading="lazy" className="h-full w-full object-cover" />
                        )}
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="truncate font-bold uppercase">{orderItem.productName}</p>
                        <p className="font-mono text-[10px] text-muted-foreground">
                          {[orderItem.size && formatSize(orderItem.size), orderItem.color].filter(Boolean).join(' · ')}
                        </p>
                      </div>
                      <span className="font-mono text-[10px] text-muted-foreground">× {item.quantity}</span>
                    </li>
                  )
                })}
              </ul>
            </div>
          )}
          <div>
            <p className="mb-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Tracking</p>
            {shipment.events.length === 0 ? (
              <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                No tracking events
              </p>
            ) : (
              <ol className="space-y-3">
                {[...shipment.events].reverse().map((event) => (
                  <li
                    key={`${event.status}-${event.occurredAt}`}
                    className="flex items-start justify-between gap-3 text-xs"
                  >
                    <div>
                      <p>{event.description}</p>
                      {event.location && (
                        <p className="font-mono text-[10px] text-muted-foreground">{event.location}</p>
                      )}
                    </div>
                    <span className="shrink-0 font-mono text-[10px] text-muted-foreground">
                      {formatDateTime(event.occurredAt)}
                    </span>
                  </li>
                ))}
              </ol>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
