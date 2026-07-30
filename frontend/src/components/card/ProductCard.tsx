import { Link } from 'react-router-dom'
import { Heart } from 'lucide-react'

import type { ProductResponse } from '@/types/Product'

import { useWishlistStore } from '@/store/useWishlistStore'

interface ProductCard {
  product: ProductResponse;
  index: number;
}

function ProductCard({ product, index }: ProductCard) {
  const offset = index % 2 === 1 ? "lg:translate-y-12" : "";

  const finalPrice = product.discountPercentage > 0 
    ? (product.price * (1 - product.discountPercentage / 100)).toFixed(2)
    : product.price.toFixed(2);

  const addToWishlist = useWishlistStore((s) => s.addtoWishlist);
    const removeFromWishlist = useWishlistStore((s) => s.removeFromWishlist);
    
    const items = useWishlistStore((s) => s.items);
    const isActive = items.some((item) => item.id === product.id);
  
    const handleToggle = (e: React.MouseEvent) => {
      e.preventDefault();
      e.stopPropagation();
  
      if (isActive) {
        removeFromWishlist(product.id);
      } else {
        addToWishlist(product);
      }
    };

  return (
    <Link to={`/product/${product.id}`} className={`group block ${offset}`}>
      <div className="relative aspect-[3/4] overflow-hidden border border-border bg-card">
        <img
          src={product.cover}
          alt={product.name}
          loading="lazy"
          className="h-full w-full object-cover grayscale-[40%] contrast-110 transition-all duration-700 group-hover:scale-105 group-hover:grayscale-0"
        />
        <div className="absolute left-3 top-3 flex flex-col gap-1">
          <span className="bg-background/70 px-2 py-1 font-mono text-[9px] font-bold uppercase tracking-widest backdrop-blur">
            REF: {product.ref}
          </span>
        </div>
        {product.discountPercentage > 0 && (
          <div className="absolute right-3 top-3 flex h-12 w-12 items-center justify-center bg-neon-pink font-mono text-xs font-black tracking-tighter text-primary-foreground shadow-lg shadow-neon-pink/30">
            -{product.discountPercentage}%
          </div>
        )}
        <div className={`absolute ${product.discountPercentage > 0 ? "right-3 top-[4.25rem]" : "right-3 top-3"}`}>
          <button
            type="button"
            onClick={handleToggle}
            aria-label={isActive ? "Remove from wishlist" : "Add to wishlist"}
            aria-pressed={isActive}
            className={`flex h-9 w-9 items-center justify-center border backdrop-blur transition-all ${
              isActive
                ? "border-neon-pink bg-neon-pink/20 text-neon-pink shadow-lg shadow-neon-pink/30"
                : "border-border bg-background/70 text-foreground/80 hover:border-neon-pink hover:text-neon-pink"
            }`}
          >
            <Heart className="h-4 w-4" fill={isActive ? "currentColor" : "none"} strokeWidth={1.5} />
          </button>
        </div>
        <div className="absolute inset-x-0 bottom-0 translate-y-full bg-neon-cyan p-3 text-center transition-transform duration-300 group-hover:translate-y-0">
          <span className="text-xs font-black uppercase tracking-widest text-primary-foreground">
            View Object
          </span>
        </div>
      </div>
      <div className="mt-4 flex items-start justify-between gap-3">
        <div>
          <h3 className="font-bold uppercase tracking-tight">{product.name}</h3>
          <p className="mt-1 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            {product.category}
          </p>
        </div>
        <div className="flex shrink-0 flex-col items-end leading-tight">
          {product.discountPercentage > 0 ? (
            <>
              <span className="font-mono text-sm tracking-tighter text-neon-pink">
                ${finalPrice}
              </span>
              <span className="font-mono text-[10px] text-muted-foreground line-through">
                ${product.price.toFixed(2)}
              </span>
            </>
          ) : (
            <span className="font-mono text-sm tracking-tighter text-neon-cyan">
              ${finalPrice}
            </span>
          )}
        </div>
      </div>
    </Link>
  )
}

export default ProductCard
