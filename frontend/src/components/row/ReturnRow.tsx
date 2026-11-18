import { ReturnStatusBadge } from '@/components/badge/ReturnStatusBadge'
import { RETURN_REASON_LABELS } from '@/constants'
import { formatDateTime, formatPrice, formatSize } from '@/utils/format'
import type { OrderItemResponse, OrderReturnResponse } from '@/types/order'

interface ReturnRowProps {
  orderReturn: OrderReturnResponse
  orderItems: OrderItemResponse[]
}

export const ReturnRow = ({ orderReturn, orderItems }: ReturnRowProps) => (
  <div className="space-y-2 border-b border-border p-3 text-xs last:border-b-0">
    <div className="flex items-center justify-between gap-3">
      <ReturnStatusBadge status={orderReturn.status} />
      <span className="font-mono text-[10px] text-muted-foreground">{formatDateTime(orderReturn.createdAt)}</span>
    </div>
    <p className="text-muted-foreground">
      {RETURN_REASON_LABELS[orderReturn.reason]}
      {orderReturn.comment && ` · ${orderReturn.comment}`}
    </p>
    {orderReturn.status === 'APPROVED' && <p className="text-neon-cyan">Approved: send the items back.</p>}
    {orderReturn.rejectionReason && <p className="text-destructive">Reason: {orderReturn.rejectionReason}</p>}
    <ul className="space-y-0.5">
      {orderReturn.items.map((returned) => {
        const item = orderItems.find((orderItem) => orderItem.id === returned.orderItemId)

        return (
          <li key={returned.orderItemId} className="flex justify-between gap-3">
            <span className="truncate">
              {[item?.productName, item?.size && formatSize(item.size), item?.color].filter(Boolean).join(' · ')}
            </span>
            <span className="font-mono">× {returned.quantity}</span>
          </li>
        )
      })}
    </ul>
    {orderReturn.refundAmount !== undefined && (
      <div className="flex justify-between border-t border-border pt-2 font-mono">
        <span className="uppercase tracking-widest text-muted-foreground">Refunded</span>
        <span className="text-neon-cyan">{formatPrice(orderReturn.refundAmount)}</span>
      </div>
    )}
  </div>
)
