import { Link } from 'react-router-dom'
import { Heart, Trash2 } from 'lucide-react'
import { useWishlist } from '@/hooks/useWishlist'
import { ProductCard } from '@/components/card/ProductCard'

export const Wishlist = () => {
  const { items, clearWishlist } = useWishlist()

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
      <div className="mt-4 flex flex-wrap items-end justify-between gap-4">
        <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">
          Your <span className="text-neon-pink">Favorites</span>
        </h1>
        {items.length > 0 && (
          <button
            onClick={() => clearWishlist()}
            className="flex items-center gap-2 border border-border px-4 py-2 text-xs font-bold uppercase tracking-widest hover:border-neon-pink hover:text-neon-pink"
          >
            <Trash2 className="h-3 w-3" /> Clear all
          </button>
        )}
      </div>
      {items.length === 0 ? (
        <div className="mt-20 flex flex-col items-center gap-6 border border-dashed border-border py-20 text-center">
          <div className="flex h-16 w-16 items-center justify-center border border-neon-pink/40 text-neon-pink">
            <Heart className="h-6 w-6" />
          </div>
          <p className="max-w-md text-muted-foreground">
            Tap the heart on any product to save it here for later.
          </p>
          <Link
            to="/shop"
            className="bg-neon-pink px-10 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80"
          >
            Enter Archive
          </Link>
        </div>
      ) : (
        <div className="mt-12 grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-3 lg:grid-cols-4 lg:gap-x-6">
          {items.map((product, i) => (
            <ProductCard key={product.id} product={product} index={i} />
          ))}
        </div>
      )}
    </div>
  )
}
