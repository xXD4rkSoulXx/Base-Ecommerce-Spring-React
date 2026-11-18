import { formatPrice } from '@/utils/format'
import { getDiscountedPrice } from '@/utils/price'

interface ProductPriceProps {
  price: number
  discountPercentage: number
}

export const ProductPrice = ({ price, discountPercentage }: ProductPriceProps) => {
  const finalPrice = getDiscountedPrice(price, discountPercentage)

  return (
    <div className="flex shrink-0 flex-col items-end leading-tight">
      {discountPercentage > 0 ? (
        <>
          <span className="font-mono text-sm tracking-tighter text-neon-pink">{formatPrice(finalPrice)}</span>
          <span className="font-mono text-[10px] text-muted-foreground line-through">{formatPrice(price)}</span>
        </>
      ) : (
        <span className="font-mono text-sm tracking-tighter text-neon-cyan">{formatPrice(finalPrice)}</span>
      )}
    </div>
  )
}
