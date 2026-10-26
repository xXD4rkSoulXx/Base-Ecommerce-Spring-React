import { ReturnStatusBadge } from '@/components/ui/ReturnStatusBadge'
import type { OrderItemResponse, OrderReturnResponse, ReturnReason } from '@/types/order'

interface ReturnRowProps {
  orderReturn: OrderReturnResponse
  orderItems: OrderItemResponse[]
}

const priceFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const dateTimeFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium', timeStyle: 'short' })
const formatSize = (size: string) => (size.startsWith('_') ? size.substring(1) : size)

const returnReasonLabels: Record<ReturnReason, string> = {
  DOES_NOT_FIT: 'Does not fit',
  DEFECTIVE: 'Defective or damaged',
  NOT_AS_DESCRIBED: 'Not as described',
  CHANGED_MIND: 'Changed my mind',
  OTHER: 'Other'
}

export const ReturnRow = ({ orderReturn, orderItems }: ReturnRowProps) => (
  <div className="space-y-2 border-b border-border p-3 text-xs last:border-b-0">
    <div className="flex items-center justify-between gap-3">
      <ReturnStatusBadge status={orderReturn.status} />
      <span className="font-mono text-[10px] text-muted-foreground">
        {dateTimeFormatter.format(new Date(orderReturn.createdAt))}
      </span>
    </div>
    <p className="text-muted-foreground">
      {returnReasonLabels[orderReturn.reason]}
      {orderReturn.comment && ` · ${orderReturn.comment}`}
    </p>
    <ul className="space-y-0.5">
      {orderReturn.items.map((returned) => {
        const item = orderItems.find((orderItem) => orderItem.id === returned.orderItemId)

        return (
          <li key={returned.orderItemId} className="flex justify-between gap-3">
            <span className="truncate">
              {[item?.productName, item?.size && formatSize(item.size), item?.color]
                .filter(Boolean)
                .join(' · ')}
            </span>
            <span className="font-mono">× {returned.quantity}</span>
          </li>
        )
      })}
    </ul>
    {orderReturn.refundAmount !== undefined && (
      <div className="flex justify-between border-t border-border pt-2 font-mono">
        <span className="uppercase tracking-widest text-muted-foreground">Refunded</span>
        <span className="text-neon-cyan">{priceFormatter.format(orderReturn.refundAmount)}</span>
      </div>
    )}
  </div>
)
