import { Link } from 'react-router-dom'
import { Heart } from 'lucide-react'
import { DiscountBadge } from '@/components/badge/DiscountBadge'
import { ProductPrice } from '@/components/ui/ProductPrice'
import { useWishlist } from '@/hooks/useWishlist'
import type { ProductResponse } from '@/types/product'

interface ProductCardProps {
  product: ProductResponse
  index: number
}

export const ProductCard = ({ product, index }: ProductCardProps) => {
  const { isInWishlist, toggleWishlist } = useWishlist()

  const isActive = isInWishlist(product.id)
  const hasDiscount = product.discountPercentage > 0

  return (
    <div className={`group relative ${index % 2 === 1 ? 'lg:translate-y-12' : ''}`}>
      <Link to={`/product/${product.id}`} className="block">
        <div className="relative aspect-[3/4] overflow-hidden border border-border bg-card">
          <img
            src={product.cover}
            alt=""
            loading="lazy"
            className="h-full w-full object-cover grayscale-[40%] contrast-110 transition-all duration-700 group-hover:scale-105 group-hover:grayscale-0"
          />
          <span className="absolute left-3 top-3 bg-background/70 px-2 py-1 font-mono text-[9px] font-bold uppercase tracking-widest backdrop-blur">
            REF: {product.ref}
          </span>
          {hasDiscount && <DiscountBadge percentage={product.discountPercentage} />}
          <div className="absolute inset-x-0 bottom-0 translate-y-full bg-neon-cyan p-3 text-center transition-transform duration-300 group-hover:translate-y-0">
            <span className="text-xs font-black uppercase tracking-widest text-primary-foreground">View Object</span>
          </div>
        </div>
        <div className="mt-4 flex items-start justify-between gap-3">
          <div>
            <h3 className="font-bold uppercase tracking-tight">{product.name}</h3>
            <p className="mt-1 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              {product.category}
            </p>
          </div>
          <ProductPrice price={product.price} discountPercentage={product.discountPercentage} />
        </div>
      </Link>
      <button
        type="button"
        onClick={() => toggleWishlist(product)}
        aria-label={isActive ? 'Remove from wishlist' : 'Add to wishlist'}
        aria-pressed={isActive}
        className={`absolute right-3 flex h-9 w-9 items-center justify-center border backdrop-blur transition-all ${
          hasDiscount ? 'top-[4.25rem]' : 'top-3'
        } ${
          isActive
            ? 'border-neon-pink bg-neon-pink/20 text-neon-pink shadow-lg shadow-neon-pink/30'
            : 'border-border bg-background/70 text-foreground/80 hover:border-neon-pink hover:text-neon-pink'
        }`}
      >
        <Heart className="h-4 w-4" fill={isActive ? 'currentColor' : 'none'} strokeWidth={1.5} />
      </button>
    </div>
  )
}
