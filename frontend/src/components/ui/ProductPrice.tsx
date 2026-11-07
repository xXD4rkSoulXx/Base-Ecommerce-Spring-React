interface ProductPriceProps {
  price: number
  discountPercentage: number
}

const priceFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const formatPrice = (amount: number) => priceFormatter.format(amount)

export const ProductPrice = ({ price, discountPercentage }: ProductPriceProps) => {
  const finalPrice = Math.round((Math.round(price * 100) * (100 - discountPercentage)) / 100) / 100

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
