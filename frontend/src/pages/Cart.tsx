import { Link } from 'react-router-dom'
import { Trash2 } from 'lucide-react'
import { CartItemRow } from '@/components/row/CartItemRow'
import { DetailLine } from '@/components/ui/DetailLine'
import { EmptyState } from '@/components/ui/EmptyState'
import { useCart } from '@/hooks/useCart'
import { formatPrice } from '@/utils/format'

export const Cart = () => {
  const { items, discountCode, subtotal, discountAmount, getLineTotal, updateQuantity, removeFromCart, clearCart } = useCart()

  const total = subtotal - discountAmount
  const hasUnavailableItems = items.some((item) => item.available === false)

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
      <div className="mt-4 flex flex-wrap items-end justify-between gap-4">
        <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">
          Your <span className="text-neon-cyan">Cart</span>
        </h1>
        {items.length > 0 && (
          <button
            type="button"
            onClick={clearCart}
            className="flex items-center gap-2 border border-border px-4 py-2 text-xs font-bold uppercase tracking-widest hover:border-neon-cyan hover:text-neon-cyan"
          >
            <Trash2 className="h-3 w-3" /> Clear all
          </button>
        )}
      </div>
      {hasUnavailableItems && (
        <div className="mt-8 border border-neon-pink/40 bg-neon-pink/5 p-4 text-sm text-neon-pink">
          Some items are no longer available. Remove them from your cart to continue to checkout.
        </div>
      )}
      {items.length === 0 ? (
        <EmptyState accent="cyan" message="No objects in transit. Browse the archive to begin." />
      ) : (
        <div className="mt-12 grid gap-12 lg:grid-cols-[1fr_400px]">
          <div className="border-t border-border">
            {items.map((item) => (
              <CartItemRow
                key={item.productSkuId}
                item={item}
                lineTotal={getLineTotal(item)}
                onQuantityChange={(quantity) => updateQuantity(item.productSkuId, quantity)}
                onRemove={() => removeFromCart(item.productSkuId)}
              />
            ))}
          </div>
          <aside className="h-fit border border-border bg-card p-6 md:p-8">
            <h2 className="mb-6 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Summary</h2>
            <div className="space-y-3 border-b border-border pb-4 text-sm">
              <DetailLine label="Subtotal" value={formatPrice(subtotal)} />
              {discountCode && (
                <DetailLine
                  label={`Discount (${discountCode})`}
                  value={`-${formatPrice(discountAmount)}`}
                  valueClassName="text-neon-pink"
                />
              )}
            </div>
            <div className="mt-4 flex items-baseline justify-between">
              <span className="font-bold uppercase">Total</span>
              <span className="font-mono text-2xl text-neon-cyan">{formatPrice(total)}</span>
            </div>
            {hasUnavailableItems ? (
              <button
                type="button"
                disabled
                className="mt-8 block w-full cursor-not-allowed bg-neon-cyan py-5 text-center text-xs font-black uppercase tracking-widest text-primary-foreground opacity-40"
              >
                Remove unavailable items
              </button>
            ) : (
              <Link
                to="/checkout"
                className="mt-8 block bg-neon-cyan py-5 text-center text-xs font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80"
              >
                Proceed to Checkout
              </Link>
            )}
            <Link
              to="/shop"
              className="mt-3 block py-3 text-center text-xs font-bold uppercase tracking-widest text-muted-foreground hover:text-foreground"
            >
              ← Continue Shopping
            </Link>
          </aside>
        </div>
      )}
    </div>
  )
}
