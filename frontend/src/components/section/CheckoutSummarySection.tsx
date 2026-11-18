import { PromoCodeForm } from '@/components/form/PromoCodeForm'
import { DetailLine } from '@/components/ui/DetailLine'
import { useCart } from '@/hooks/useCart'
import { getErrorMessage } from '@/utils/error'
import { formatPrice, formatSize } from '@/utils/format'

interface CheckoutSummarySectionProps {
  shippingAmount?: number
}

export const CheckoutSummarySection = ({ shippingAmount }: CheckoutSummarySectionProps) => {
  const {
    items,
    subtotal,
    discountAmount,
    discountCode,
    isDiscountCodeError,
    discountCodeError,
    applyDiscountCode,
    getLineTotal,
    removeDiscountCode
  } = useCart()

  const total = subtotal - discountAmount + (shippingAmount ?? 0)

  return (
    <aside className="h-fit border border-border bg-card p-6">
      <h2 className="mb-4 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Order Manifest</h2>
      <div className="space-y-3 border-b border-border pb-4">
        {items.map((item) => (
          <div key={item.productSkuId} className="flex gap-3 text-sm">
            <div className="h-14 w-14 shrink-0 overflow-hidden border border-border">
              <img src={item.cover} alt={item.productName} className="h-full w-full object-cover" />
            </div>
            <div className="flex-1">
              <p className="text-xs font-bold uppercase">{item.productName}</p>
              <p className="font-mono text-[10px] text-muted-foreground">
                {formatSize(item.size)} · Qty {item.quantity}
              </p>
            </div>
            <span className="font-mono text-xs text-neon-cyan">{formatPrice(getLineTotal(item))}</span>
          </div>
        ))}
      </div>
      <div className="mt-4 space-y-2 text-sm">
        <DetailLine label="Subtotal" value={formatPrice(subtotal)} />
        {discountCode && (
          <DetailLine
            label={`Discount (${discountCode})`}
            value={`-${formatPrice(discountAmount)}`}
            valueClassName="text-neon-pink"
          />
        )}
        <DetailLine
          label="Shipping"
          value={shippingAmount === undefined ? 'Next step' : formatPrice(shippingAmount)}
        />
        <DetailLine label="Taxes" value="At payment" />
        <div className="mt-4">
          {discountCode ? (
            <div className="flex items-center justify-between border border-border px-3 py-2">
              <span className="font-mono text-xs uppercase tracking-widest text-neon-pink">{discountCode}</span>
              <button
                type="button"
                onClick={removeDiscountCode}
                className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground hover:text-foreground"
              >
                Remove
              </button>
            </div>
          ) : (
            <PromoCodeForm
              onApply={applyDiscountCode}
              error={isDiscountCodeError ? getErrorMessage(discountCodeError, 'Invalid discount code') : null}
            />
          )}
        </div>
        <div className="mt-3 flex justify-between border-t border-border pt-3">
          <span className="font-bold uppercase">Total before tax</span>
          <span className="font-mono text-xl text-neon-cyan">{formatPrice(total)}</span>
        </div>
      </div>
    </aside>
  )
}
