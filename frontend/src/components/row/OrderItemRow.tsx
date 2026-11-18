import { formatPrice, formatSize } from '@/utils/format'
import type { OrderItemResponse } from '@/types/order'

interface OrderItemRowProps {
  item: OrderItemResponse
}

export const OrderItemRow = ({ item }: OrderItemRowProps) => (
  <div className="flex items-center justify-between gap-3 border-b border-border p-3 last:border-b-0">
    <div className="flex min-w-0 items-center gap-3">
      <div className="h-14 w-14 shrink-0 overflow-hidden border border-border bg-background">
        {item.imagePath && (
          <img src={item.imagePath} alt="" loading="lazy" className="h-full w-full object-cover" />
        )}
      </div>
      <div className="min-w-0">
        <p className="font-mono text-[10px] text-neon-cyan">{item.ref}</p>
        <p className="truncate font-bold uppercase">{item.productName}</p>
        <p className="font-mono text-[10px] text-muted-foreground">
          {[item.size && formatSize(item.size), item.color, `Qty ${item.quantity}`].filter(Boolean).join(' · ')}
        </p>
      </div>
    </div>
    <span className="font-mono text-sm text-neon-cyan">{formatPrice(item.lineTotal)}</span>
  </div>
)
