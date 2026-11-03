import { Link } from 'react-router-dom'
import { MapPin } from 'lucide-react'
import { ReturnStatusBadge } from '@/components/ui/ReturnStatusBadge'
import type { AdminReturnResponse } from '@/types/admin/return'
import type { ReturnReason, ShippingAddress } from '@/types/order'

interface AdminReturnCardProps {
  orderReturn: AdminReturnResponse
  canEdit: boolean
  disabled: boolean
  onApprove: (id: number) => void
  onReject: (orderReturn: AdminReturnResponse) => void
  onReceive: (id: number) => void
  onRefund: (id: number) => void
}

const priceFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const dateTimeFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium', timeStyle: 'short' })
const countryNames = new Intl.DisplayNames('en', { type: 'region' })
const formatSize = (size: string) => (size.startsWith('_') ? size.substring(1) : size)

const formatAddress = (address: ShippingAddress) =>
  [
    address.recipientName,
    [address.addressLine1, address.addressLine2].filter(Boolean).join(', '),
    [address.postalCode, address.city].filter(Boolean).join(' '),
    countryNames.of(address.country) ?? address.country,
    address.phone
  ]
    .filter(Boolean)
    .join(' · ')

const returnReasonLabels: Record<ReturnReason, string> = {
  DOES_NOT_FIT: 'Does not fit',
  DEFECTIVE: 'Defective or damaged',
  NOT_AS_DESCRIBED: 'Not as described',
  CHANGED_MIND: 'Changed my mind',
  OTHER: 'Other'
}

const primaryButtonClass =
  'border border-neon-cyan/50 px-4 py-2 font-mono text-[10px] uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan/10 disabled:opacity-50'

export const AdminReturnCard = ({
  orderReturn,
  canEdit,
  disabled,
  onApprove,
  onReject,
  onReceive,
  onRefund
}: AdminReturnCardProps) => (
  <div className="border border-border bg-card p-5">
    <div className="flex flex-wrap items-start justify-between gap-3">
      <div>
        <div className="flex items-center gap-3">
          <ReturnStatusBadge status={orderReturn.status} />
          <Link
            to={`/admin/orders/${orderReturn.orderId}`}
            className="font-mono text-xs text-neon-cyan hover:underline"
          >
            Order #{orderReturn.orderId}
          </Link>
        </div>
        <p className="mt-2 text-sm">{orderReturn.customerEmail}</p>
        <p className="font-mono text-[10px] text-muted-foreground">
          {dateTimeFormatter.format(new Date(orderReturn.createdAt))}
        </p>
        <p className="mt-2 flex items-start gap-2 text-xs text-muted-foreground">
          <MapPin className="mt-0.5 h-3.5 w-3.5 shrink-0 text-neon-cyan" />
          <span>{formatAddress(orderReturn.address)}</span>
        </p>
      </div>
      {canEdit && orderReturn.status === 'REQUESTED' && (
        <div className="flex gap-2">
          <button
            onClick={() => onReject(orderReturn)}
            disabled={disabled}
            className="border border-neon-pink/50 px-4 py-2 font-mono text-[10px] uppercase tracking-widest text-neon-pink hover:bg-neon-pink/10 disabled:opacity-50"
          >
            Reject
          </button>
          <button
            onClick={() => onApprove(orderReturn.id)}
            disabled={disabled}
            className={primaryButtonClass}
          >
            Approve
          </button>
        </div>
      )}
      {canEdit && orderReturn.status === 'APPROVED' && (
        <button
          onClick={() => onReceive(orderReturn.id)}
          disabled={disabled}
          className={primaryButtonClass}
        >
          Mark as received
        </button>
      )}
      {canEdit && orderReturn.status === 'RECEIVED' && (
        <button
          onClick={() => onRefund(orderReturn.id)}
          disabled={disabled}
          className={primaryButtonClass}
        >
          Issue refund
        </button>
      )}
    </div>
    <p className="mt-4 text-xs text-muted-foreground">
      {returnReasonLabels[orderReturn.reason]}
      {orderReturn.comment && ` · ${orderReturn.comment}`}
    </p>
    <ul className="mt-3 space-y-2">
      {orderReturn.items.map((item, index) => (
        <li key={index} className="flex items-center gap-3 text-xs">
          <div className="h-12 w-12 shrink-0 overflow-hidden border border-border bg-background">
            {item.imagePath && (
              <img
                src={item.imagePath}
                alt={item.productName}
                loading="lazy"
                className="h-full w-full object-cover"
              />
            )}
          </div>
          <span className="min-w-0 flex-1 truncate">
            {[item.productName, item.size && formatSize(item.size), item.color]
              .filter(Boolean)
              .join(' · ')}
          </span>
          <span className="font-mono">× {item.quantity}</span>
        </li>
      ))}
    </ul>
    {orderReturn.refundAmount !== undefined && (
      <div className="mt-3 flex justify-between border-t border-border pt-3 font-mono text-xs">
        <span className="uppercase tracking-widest text-muted-foreground">Refunded</span>
        <span className="text-neon-cyan">{priceFormatter.format(orderReturn.refundAmount)}</span>
      </div>
    )}
    {orderReturn.rejectionReason && (
      <p className="mt-3 border-t border-border pt-3 text-xs text-red-500">
        Reason: {orderReturn.rejectionReason}
      </p>
    )}
  </div>
)
