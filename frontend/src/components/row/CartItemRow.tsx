import { Link } from 'react-router-dom'
import { Minus, Plus, X } from 'lucide-react'
import { StatusBadge } from '@/components/badge/StatusBadge'
import { MAX_CART_ITEM_QUANTITY } from '@/constants'
import type { GuestCartItem } from '@/store/useCartStore'
import { formatPrice, formatSize } from '@/utils/format'

interface CartItemRowProps {
  item: GuestCartItem
  lineTotal: number
  onQuantityChange: (quantity: number) => void
  onRemove: () => void
}

export const CartItemRow = ({ item, lineTotal, onQuantityChange, onRemove }: CartItemRowProps) => {
  const isUnavailable = item.available === false
  const maxQuantity = Math.min(item.stock, MAX_CART_ITEM_QUANTITY)

  return (
    <div className={`flex gap-4 border-b border-border py-6 md:gap-6 ${isUnavailable ? 'opacity-60' : ''}`}>
      <Link to={`/product/${item.productId}`} className="block w-24 shrink-0 md:w-32">
        <div className="aspect-square overflow-hidden border border-border bg-card">
          <img src={item.cover} alt={item.productName} loading="lazy" className="h-full w-full object-cover" />
        </div>
      </Link>
      <div className="flex flex-1 flex-col">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">{item.productRef}</p>
            <Link
              to={`/product/${item.productId}`}
              className="text-base font-bold uppercase hover:text-neon-cyan md:text-lg"
            >
              {item.productName}
            </Link>
            <div className="mt-1 flex flex-wrap items-center gap-3 font-mono text-xs text-muted-foreground">
              <span>
                Size: <span className="text-foreground">{formatSize(item.size)}</span>
              </span>
              <span className="flex items-center gap-1.5">
                Color:
                <span
                  className="inline-block h-3 w-3 border border-border align-middle"
                  style={{ backgroundColor: item.color.toLowerCase() }}
                />
                <span className="text-foreground">{item.color}</span>
              </span>
            </div>
            {isUnavailable && (
              <div className="mt-2">
                <StatusBadge tone="pink">Unavailable</StatusBadge>
              </div>
            )}
          </div>
          <button
            type="button"
            onClick={onRemove}
            aria-label={`Remove ${item.productName} from the cart`}
            className="text-muted-foreground hover:text-destructive"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
        <div className="mt-auto flex items-end justify-between">
          {!isUnavailable && (
            <div className="flex items-center border border-border">
              <button
                type="button"
                onClick={() => onQuantityChange(item.quantity - 1)}
                disabled={item.quantity <= 1}
                aria-label="Decrease quantity"
                className="px-3 py-2 hover:bg-card disabled:cursor-not-allowed disabled:opacity-30"
              >
                <Minus className="h-3 w-3" />
              </button>
              <span className="min-w-[2.5rem] text-center font-mono text-sm">{item.quantity}</span>
              <button
                type="button"
                onClick={() => onQuantityChange(item.quantity + 1)}
                disabled={item.quantity >= maxQuantity}
                aria-label="Increase quantity"
                className="px-3 py-2 hover:bg-card disabled:cursor-not-allowed disabled:opacity-30"
              >
                <Plus className="h-3 w-3" />
              </button>
            </div>
          )}
          <span
            className={`ml-auto font-mono text-lg ${
              isUnavailable ? 'text-muted-foreground line-through' : 'text-neon-cyan'
            }`}
          >
            {formatPrice(lineTotal)}
          </span>
        </div>
      </div>
    </div>
  )
}
