import { useState } from 'react'
import { Activity, Box, Clock, MapPin, Package, RotateCcw, Truck } from 'lucide-react'
import { OrderStatusBadge } from '@/components/badge/OrderStatusBadge'
import { ReturnDialog } from '@/components/dialog/ReturnDialog'
import { OrderItemRow } from '@/components/row/OrderItemRow'
import { ParcelRow } from '@/components/row/ParcelRow'
import { ReturnRow } from '@/components/row/ReturnRow'
import { InfoBox } from '@/components/ui/InfoBox'
import { Modal } from '@/components/ui/Modal'
import { OrderAddressSection } from '@/components/section/OrderAddressSection'
import { OrderTimelineSection } from '@/components/section/OrderTimelineSection'
import { OrderTotalsSection } from '@/components/section/OrderTotalsSection'
import { OrderRowsSection } from '@/components/section/OrderRowsSection'
import { SectionTitle } from '@/components/ui/SectionTitle'
import { useOrder } from '@/hooks/useOrder'
import { formatDate, formatDateTime } from '@/utils/format'

interface OrderDetailDialogProps {
  orderId: number | null
  onClose: () => void
}

export const OrderDetailDialog = ({ orderId, onClose }: OrderDetailDialogProps) => {
  const { data: order, isLoading } = useOrder(orderId)
  const [returnOpen, setReturnOpen] = useState(false)

  if (orderId === null) {
    return null
  }

  return (
    <>
      <Modal
        eyebrow="Order"
        title={`#${orderId}`}
        description={order ? `${formatDateTime(order.createdAt)} · ${order.recipientName}` : undefined}
        size="2xl"
        onClose={onClose}
      >
        {isLoading && <p className="font-mono text-xs text-muted-foreground">Loading manifest...</p>}
        {!isLoading && !order && (
          <p className="font-mono text-xs uppercase tracking-widest text-neon-pink">Failed to load order</p>
        )}
        {order && (
          <div className="space-y-6">
            <div className="grid gap-3 sm:grid-cols-3">
              <InfoBox icon={<Activity className="h-3.5 w-3.5" />} label="Status">
                <OrderStatusBadge status={order.status} />
              </InfoBox>
              <InfoBox icon={<MapPin className="h-3.5 w-3.5" />} label="Ship To">
                <OrderAddressSection address={order} />
              </InfoBox>
              <InfoBox icon={<Truck className="h-3.5 w-3.5" />} label="Carrier">
                <div className="space-y-0.5 text-xs">
                  <p>{order.shippingCarrier || '—'}</p>
                  {order.shippingService && <p className="text-muted-foreground">{order.shippingService}</p>}
                </div>
              </InfoBox>
            </div>
            {order.events.length > 0 && (
              <div>
                <SectionTitle as="h3" icon={<Clock className="h-3.5 w-3.5" />}>
                  Timeline
                </SectionTitle>
                <OrderTimelineSection events={order.events} />
              </div>
            )}
            {order.shipments.length > 0 && (
              <div>
                <SectionTitle as="h3" icon={<Box className="h-3.5 w-3.5" />}>
                  Parcels ({order.shipments.length})
                </SectionTitle>
                <div className="max-h-72 overflow-y-auto border border-border">
                  {order.shipments.map((shipment, index) => (
                    <ParcelRow key={shipment.id} shipment={shipment} index={index} orderItems={order.items} />
                  ))}
                </div>
              </div>
            )}
            <div>
              <SectionTitle
                as="h3"
                icon={<Package className="h-3.5 w-3.5" />}
                action={
                  order.returnableUntil ? (
                    <button
                      type="button"
                      onClick={() => setReturnOpen(true)}
                      className="flex items-center gap-1.5 border border-neon-cyan/40 px-3 py-1.5 font-mono text-[10px] uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan/10"
                    >
                      <RotateCcw className="h-3 w-3" />
                      Return items
                    </button>
                  ) : undefined
                }
              >
                Items ({order.items.length})
              </SectionTitle>
              {order.returnableUntil && (
                <p className="mb-3 font-mono text-[10px] text-muted-foreground">
                  Returns accepted until {formatDate(order.returnableUntil)}
                </p>
              )}
              <div className="border border-border">
                {order.items.map((item) => (
                  <OrderItemRow key={item.id} item={item} />
                ))}
              </div>
            </div>
            {order.returns.length > 0 && (
              <OrderRowsSection
                as="h3"
                icon={<RotateCcw className="h-3.5 w-3.5" />}
                title="Returns"
                count={order.returns.length}
              >
                {order.returns.map((orderReturn) => (
                  <ReturnRow key={orderReturn.id} orderReturn={orderReturn} orderItems={order.items} />
                ))}
              </OrderRowsSection>
            )}
            <OrderTotalsSection order={order} />
          </div>
        )}
      </Modal>
      {returnOpen && order && <ReturnDialog order={order} onClose={() => setReturnOpen(false)} />}
    </>
  )
}
