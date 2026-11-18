import { Link } from 'react-router-dom'
import { DiscountBadge } from '@/components/badge/DiscountBadge'
import { ProductPrice } from '@/components/ui/ProductPrice'
import type { ProductResponse } from '@/types/product'

interface FeaturedProductCardProps {
  product: ProductResponse
  secondary?: boolean
}

export const FeaturedProductCard = ({ product, secondary = false }: FeaturedProductCardProps) => (
  <Link to={`/product/${product.id}`} className={`block ${secondary ? 'lg:translate-y-24' : ''}`}>
    <div className="relative aspect-[3/4] overflow-hidden border border-border bg-card">
      <img
        src={product.cover}
        alt=""
        loading={secondary ? 'lazy' : undefined}
        className={`h-full w-full object-cover ${secondary ? 'grayscale-[30%]' : ''}`}
      />
      <div className="absolute left-3 top-3 bg-background/70 px-2 py-1 font-mono text-[9px] font-bold backdrop-blur">
        REF: {product.ref}
      </div>
      {product.discountPercentage > 0 && <DiscountBadge percentage={product.discountPercentage} />}
    </div>
    <div className="mt-4 flex items-start justify-between gap-3 text-xs">
      <span className="font-bold uppercase">{product.name}</span>
      <ProductPrice price={product.price} discountPercentage={product.discountPercentage} />
    </div>
  </Link>
)
