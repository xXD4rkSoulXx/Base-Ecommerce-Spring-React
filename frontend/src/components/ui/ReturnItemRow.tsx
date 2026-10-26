import { Minus, Plus } from 'lucide-react'
import type { OrderItemResponse } from '@/types/order'

const formatSize = (size: string) => (size.startsWith('_') ? size.substring(1) : size)

interface ReturnItemRowProps {
  item: OrderItemResponse
  quantity: number
  onChange: (quantity: number) => void
}

export const ReturnItemRow = ({ item, quantity, onChange }: ReturnItemRowProps) => (
  <div className="flex items-center justify-between gap-3 border-b border-border p-3 last:border-b-0">
    <div className="flex min-w-0 items-center gap-3">
      <div className="h-14 w-14 shrink-0 overflow-hidden border border-border bg-background">
        {item.imagePath && (
          <img
            src={item.imagePath}
            alt={item.productName}
            loading="lazy"
            className="h-full w-full object-cover"
          />
        )}
      </div>
      <div className="min-w-0">
        <p className="truncate font-bold uppercase">{item.productName}</p>
        <p className="font-mono text-[10px] text-muted-foreground">
          {[item.size && `Size ${formatSize(item.size)}`, item.color, `${item.returnableQuantity} returnable`]
            .filter(Boolean)
            .join(' · ')}
        </p>
      </div>
    </div>
    <div className="flex shrink-0 items-center gap-2 font-mono text-sm">
      <button
        type="button"
        onClick={() => onChange(quantity - 1)}
        disabled={quantity <= 0}
        className="border border-border p-1 hover:border-neon-cyan disabled:opacity-30"
        aria-label="Decrease quantity"
      >
        <Minus className="h-3 w-3" />
      </button>
      <span className="w-5 text-center">{quantity}</span>
      <button
        type="button"
        onClick={() => onChange(quantity + 1)}
        disabled={quantity >= item.returnableQuantity}
        className="border border-border p-1 hover:border-neon-cyan disabled:opacity-30"
        aria-label="Increase quantity"
      >
        <Plus className="h-3 w-3" />
      </button>
    </div>
  </div>
)
