import { Link } from 'react-router-dom'
import { X, Minus, Plus, Trash2 } from 'lucide-react'
import { useCart } from '@/hooks/useCart'

const priceFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const formatPrice = (amount: number) => priceFormatter.format(amount)

export const Cart = () => {
  const {
    items,
    discountCode,
    subtotal,
    discountAmount,
    getLineTotal,
    updateQuantity,
    removeFromCart,
    clearCart
  } = useCart()

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
            onClick={() => clearCart()}
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
        <div className="mt-20 flex flex-col items-center gap-6 border border-dashed border-border py-20 text-center">
          <p className="max-w-md text-muted-foreground">
            No objects in transit. Browse the archive to begin.
          </p>
          <Link
            to="/shop"
            className="bg-neon-cyan px-10 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80"
          >
            Enter Archive
          </Link>
        </div>
      ) : (
        <div className="mt-12 grid gap-12 lg:grid-cols-[1fr_400px]">
          <div className="border-t border-border">
            {items.map((item) => {
              const isUnavailable = item.available === false

              return (
                <div
                  key={item.productSkuId}
                  className={`flex gap-4 border-b border-border py-6 md:gap-6 ${isUnavailable ? 'opacity-60' : ''}`}
                >
                  <Link to={`/product/${item.productId}`} className="block w-24 shrink-0 md:w-32">
                    <div className="aspect-square overflow-hidden border border-border bg-card">
                      <img src={item.cover} alt={item.productName} loading="lazy" className="h-full w-full object-cover" />
                    </div>
                  </Link>
                  <div className="flex flex-1 flex-col">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                          {item.productRef}
                        </p>
                        <Link to={`/product/${item.productId}`} className="text-base font-bold uppercase hover:text-neon-cyan md:text-lg">
                          {item.productName}
                        </Link>
                        <div className="mt-1 flex flex-wrap items-center gap-3 font-mono text-xs text-muted-foreground">
                          <span>Size: <span className="text-foreground">
                            {item.size.startsWith('_') ? item.size.substring(1) : item.size}
                          </span></span>
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
                          <span className="mt-2 inline-block border border-neon-pink px-2 py-0.5 font-mono text-[10px] uppercase tracking-widest text-neon-pink">
                            Unavailable
                          </span>
                        )}
                      </div>
                      <button onClick={() => removeFromCart(item.productSkuId)} className="text-muted-foreground hover:text-destructive">
                        <X className="h-4 w-4" />
                      </button>
                    </div>
                    <div className="mt-auto flex items-end justify-between">
                      {isUnavailable ? (
                        <span />
                      ) : (
                        <div className="flex items-center border border-border">
                          <button
                            onClick={() => updateQuantity(item.productSkuId, item.quantity - 1)}
                            disabled={item.quantity <= 1}
                            className="px-3 py-2 hover:bg-card disabled:cursor-not-allowed disabled:opacity-30"
                          >
                            <Minus className="h-3 w-3" />
                          </button>
                          <span className="min-w-[2.5rem] text-center font-mono text-sm">{item.quantity}</span>
                          <button
                            onClick={() => updateQuantity(item.productSkuId, item.quantity + 1)}
                            disabled={item.quantity >= Math.min(item.stock, 10)}
                            className="px-3 py-2 hover:bg-card disabled:cursor-not-allowed disabled:opacity-30"
                          >
                            <Plus className="h-3 w-3" />
                          </button>
                        </div>
                      )}
                      <span
                        className={`font-mono text-lg ${
                          isUnavailable ? 'text-muted-foreground line-through' : 'text-neon-cyan'
                        }`}
                      >
                        {formatPrice(getLineTotal(item))}
                      </span>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
          <aside className="h-fit border border-border bg-card p-6 md:p-8">
            <h2 className="mb-6 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
              Summary
            </h2>
            <div className="space-y-3 border-b border-border pb-4 text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Subtotal</span>
                <span className="font-mono">{formatPrice(subtotal)}</span>
              </div>
              {discountCode && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">
                    Discount ({discountCode})
                  </span>
                  <span className="font-mono text-neon-pink">-{formatPrice(discountAmount)}</span>
                </div>
              )}
            </div>
            <div className="mt-4 flex items-baseline justify-between">
              <span className="font-bold uppercase">Total</span>
              <span className="font-mono text-2xl text-neon-cyan">
                {formatPrice(total)}
              </span>
            </div>
            {hasUnavailableItems ? (
              <button
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
