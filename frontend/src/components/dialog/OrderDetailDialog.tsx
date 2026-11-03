import { useState } from 'react'
import { Activity, Box, Clock, MapPin, Package, RotateCcw, Truck, X } from 'lucide-react'
import { InfoBox } from '@/components/ui/InfoBox'
import { OrderStatusBadge } from '@/components/ui/OrderStatusBadge'
import { ParcelRow } from '@/components/ui/ParcelRow'
import { OrderTimeline } from '@/components/ui/OrderTimeline'
import { OrderAddress } from '@/components/ui/OrderAddress'
import { OrderItemRow } from '@/components/ui/OrderItemRow'
import { OrderTotals } from '@/components/ui/OrderTotals'
import { ReturnRow } from '@/components/ui/ReturnRow'
import { ReturnDialog } from '@/components/dialog/ReturnDialog'
import { useOrder } from '@/hooks/useOrder'

interface OrderDetailDialogProps {
  orderId: number | null
  onClose: () => void
}

const dateTimeFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium', timeStyle: 'short' })
const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium' })

export const OrderDetailDialog = ({ orderId, onClose }: OrderDetailDialogProps) => {
  const { data: order, isLoading, isError } = useOrder(orderId)
  const [returnOpen, setReturnOpen] = useState(false)

  if (orderId === null) {
    return null
  }

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center p-4" aria-modal>
      <div
        onClick={onClose}
        className="absolute inset-0 bg-background/85 backdrop-blur-sm"
      />
      <div className="relative z-10 flex max-h-[90vh] w-full max-w-2xl flex-col border border-neon-cyan/30 bg-card shadow-2xl shadow-neon-cyan/10">
        <div className="flex items-start justify-between border-b border-border px-5 py-4">
          <div>
            <h3 className="mt-1 font-display text-2xl uppercase tracking-tighter">#{orderId}</h3>
            {order && (
              <p className="mt-1 font-mono text-xs text-muted-foreground">
                {dateTimeFormatter.format(new Date(order.createdAt))} · {order.recipientName}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            className="text-muted-foreground hover:text-foreground"
            aria-label="Close"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
        <div className="flex-1 space-y-6 overflow-y-auto p-5">
          {isLoading ? (
            <p className="font-mono text-xs text-muted-foreground">Loading manifest...</p>
          ) : isError || !order ? (
            <p className="font-mono text-xs uppercase tracking-widest text-neon-pink">
              Failed to load order
            </p>
          ) : (
            <>
              <div className="grid gap-3 sm:grid-cols-3">
                <InfoBox icon={<Activity className="h-3.5 w-3.5" />} label="Status">
                  <OrderStatusBadge status={order.status} />
                </InfoBox>
                <InfoBox icon={<MapPin className="h-3.5 w-3.5" />} label="Ship To">
                  <OrderAddress address={order} />
                </InfoBox>
                <InfoBox icon={<Truck className="h-3.5 w-3.5" />} label="Carrier">
                  <div className="space-y-0.5 text-xs">
                    <p>{order.shippingCarrier || '—'}</p>
                    {order.shippingService && (
                      <p className="text-muted-foreground">{order.shippingService}</p>
                    )}
                  </div>
                </InfoBox>
              </div>
              {order.events.length > 0 && (
                <div>
                  <div className="mb-3 flex items-center gap-2">
                    <Clock className="h-3.5 w-3.5 text-neon-cyan" />
                    <h4 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                      Timeline
                    </h4>
                  </div>
                  <OrderTimeline events={order.events} />
                </div>
              )}
              {order.shipments.length > 0 && (
                <div>
                  <div className="mb-3 flex items-center gap-2">
                    <Box className="h-3.5 w-3.5 text-neon-cyan" />
                    <h4 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                      Parcels ({order.shipments.length})
                    </h4>
                  </div>
                  <div className="max-h-72 overflow-y-auto border border-border">
                    {order.shipments.map((shipment, index) => (
                      <ParcelRow key={shipment.id} shipment={shipment} index={index} orderItems={order.items} />
                    ))}
                  </div>
                </div>
              )}
              <div>
                <div className="mb-3 flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <Package className="h-3.5 w-3.5 text-neon-cyan" />
                    <h4 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                      Items ({order.items.length})
                    </h4>
                  </div>
                  {order.returnableUntil && (
                    <button
                      type="button"
                      onClick={() => setReturnOpen(true)}
                      className="flex items-center gap-1.5 border border-neon-cyan/40 px-3 py-1.5 font-mono text-[10px] uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan/10"
                    >
                      <RotateCcw className="h-3 w-3" />
                      Return items
                    </button>
                  )}
                </div>
                {order.returnableUntil && (
                  <p className="mb-3 font-mono text-[10px] text-muted-foreground">
                    Returns accepted until {dateFormatter.format(new Date(order.returnableUntil))}
                  </p>
                )}
                <div className="border border-border">
                  {order.items.map((item) => (
                    <OrderItemRow key={item.id} item={item} />
                  ))}
                </div>
              </div>
              {order.returns.length > 0 && (
                <div>
                  <div className="mb-3 flex items-center gap-2">
                    <RotateCcw className="h-3.5 w-3.5 text-neon-cyan" />
                    <h4 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                      Returns ({order.returns.length})
                    </h4>
                  </div>
                  <div className="border border-border">
                    {order.returns.map((orderReturn) => (
                      <ReturnRow key={orderReturn.id} orderReturn={orderReturn} orderItems={order.items} />
                    ))}
                  </div>
                </div>
              )}
              <OrderTotals order={order} />
            </>
          )}
        </div>
      </div>
      {returnOpen && order && (
        <ReturnDialog order={order} onClose={() => setReturnOpen(false)} />
      )}
    </div>
  )
}
