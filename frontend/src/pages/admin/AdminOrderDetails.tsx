import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { Activity, ArrowLeft, Box, Clock, MapPin, Package, RotateCcw, Truck, User } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { InfoBox } from '@/components/ui/InfoBox'
import { OrderAddress } from '@/components/ui/OrderAddress'
import { OrderItemRow } from '@/components/ui/OrderItemRow'
import { OrderStatusBadge } from '@/components/ui/OrderStatusBadge'
import { OrderTimeline } from '@/components/ui/OrderTimeline'
import { OrderTotals } from '@/components/ui/OrderTotals'
import { ParcelRow } from '@/components/ui/ParcelRow'
import { ReturnRow } from '@/components/ui/ReturnRow'
import { SectionTitle } from '@/components/ui/SectionTitle'
import { AdminParcelActions } from '@/components/ui/admin/AdminParcelActions'
import { usePermission } from '@/hooks/usePermission'
import { useAdminOrder } from '@/hooks/admin/useAdminOrder'
import { useAdminChangeOrderStatus } from '@/hooks/admin/useAdminChangeOrderStatus'
import { useAdminChangeShipmentStatus } from '@/hooks/admin/useAdminChangeShipmentStatus'
import type { OrderStatus, ShipmentStatus } from '@/types/order'

const NEXT_STATUS_LABELS: Partial<Record<OrderStatus, string>> = {
  PROCESSING: 'Start processing'
}

const dateTimeFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium', timeStyle: 'short' })

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminOrderDetails = () => {
  const id = Number(useParams().id)
  const { data, isLoading, isError } = useAdminOrder(id)
  const { mutate: changeStatus, isPending } = useAdminChangeOrderStatus(id)
  const { mutate: changeShipmentStatus, isPending: isShipmentPending } = useAdminChangeShipmentStatus(id)
  const canEdit = usePermission('ORDERS_WRITE')
  const [error, setError] = useState<string | null>(null)

  if (isLoading) {
    return (
      <p className="p-6 font-mono text-xs uppercase tracking-widest text-muted-foreground md:p-10">
        Loading...
      </p>
    )
  }

  if (isError || !data) {
    return (
      <div className="p-6 md:p-10">
        <FormError message="Could not load the order" />
      </div>
    )
  }

  const { order, customerName, customerEmail, nextStatus, shipmentActions } = data

  const advance = (status: OrderStatus) => {
    setError(null)

    changeStatus(status, {
      onSuccess: () => toast.success(`Order #${id} moved to ${status.replace(/_/g, ' ')}`),
      onError: (mutationError) =>
        setError(getErrorMessage(mutationError, 'Could not update the order'))
    })
  }

  const changeParcel = (shipmentId: number, status: ShipmentStatus) => {
    setError(null)

    changeShipmentStatus(
      { shipmentId, status },
      {
        onSuccess: () => toast.success(`Parcel moved to ${status.replace(/_/g, ' ')}`),
        onError: (mutationError) =>
          setError(getErrorMessage(mutationError, 'Could not update the parcel'))
      }
    )
  }

  const busy = isPending || isShipmentPending
  const hasNoParcels = order.shipments.length === 0 && ['PROCESSING', 'SHIPPED'].includes(order.status)

  return (
    <div className="p-6 md:p-10">
      <Link
        to="/admin/orders"
        className="mb-6 inline-flex items-center gap-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Orders
      </Link>
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">
            Order #{order.id}
          </h1>
          <p className="mt-2 font-mono text-xs text-muted-foreground">
            {dateTimeFormatter.format(new Date(order.createdAt))}
          </p>
        </div>
        {canEdit && nextStatus && (
          <button
            onClick={() => advance(nextStatus)}
            disabled={busy}
            className="bg-neon-cyan px-5 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            {NEXT_STATUS_LABELS[nextStatus]}
          </button>
        )}
      </div>
      {error && (
        <div className="mb-6">
          <FormError message={error} />
        </div>
      )}
      {hasNoParcels && (
        <p className="mb-6 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          No parcels were created for this order. It is handled by the simulation.
        </p>
      )}
      <div className="space-y-8">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <InfoBox icon={<Activity className="h-3.5 w-3.5" />} label="Status">
            <OrderStatusBadge status={order.status} />
          </InfoBox>
          <InfoBox icon={<User className="h-3.5 w-3.5" />} label="Customer">
            <div className="space-y-0.5 text-xs">
              <p>{customerName}</p>
              <p className="text-muted-foreground">{customerEmail}</p>
            </div>
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
            <SectionTitle icon={<Clock className="h-3.5 w-3.5" />}>Timeline</SectionTitle>
            <OrderTimeline events={order.events} />
          </div>
        )}
        {order.shipments.length > 0 && (
          <div>
            <SectionTitle icon={<Box className="h-3.5 w-3.5" />}>
              Parcels ({order.shipments.length})
            </SectionTitle>
            <div className="border border-border">
              {order.shipments.map((shipment, index) => (
                <ParcelRow
                  key={shipment.id}
                  shipment={shipment}
                  index={index}
                  orderItems={order.items}
                  actions={
                    canEdit && shipmentActions[shipment.id]?.length > 0 ? (
                      <AdminParcelActions
                        actions={shipmentActions[shipment.id]}
                        disabled={busy}
                        onChange={(status) => changeParcel(shipment.id, status)}
                      />
                    ) : undefined
                  }
                />
              ))}
            </div>
          </div>
        )}
        <div>
          <SectionTitle icon={<Package className="h-3.5 w-3.5" />}>
            Items ({order.items.length})
          </SectionTitle>
          <div className="border border-border">
            {order.items.map((item) => (
              <OrderItemRow key={item.id} item={item} />
            ))}
          </div>
        </div>
        {order.returns.length > 0 && (
          <div>
            <SectionTitle icon={<RotateCcw className="h-3.5 w-3.5" />}>
              Returns ({order.returns.length})
            </SectionTitle>
            <div className="border border-border">
              {order.returns.map((orderReturn) => (
                <ReturnRow key={orderReturn.id} orderReturn={orderReturn} orderItems={order.items} />
              ))}
            </div>
          </div>
        )}
        <OrderTotals order={order} />
      </div>
    </div>
  )
}
