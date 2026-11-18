import { Heart, Trash2 } from 'lucide-react'
import { ProductCard } from '@/components/card/ProductCard'
import { EmptyState } from '@/components/ui/EmptyState'
import { FormError } from '@/components/ui/FormError'
import { useWishlist } from '@/hooks/useWishlist'

export const Wishlist = () => {
  const { items, isLoading, isError, clearWishlist } = useWishlist()

  const isEmpty = !isLoading && !isError && items.length === 0

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
      <div className="mt-4 flex flex-wrap items-end justify-between gap-4">
        <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">
          Your <span className="text-neon-pink">Favorites</span>
        </h1>
        {items.length > 0 && (
          <button
            type="button"
            onClick={clearWishlist}
            className="flex items-center gap-2 border border-border px-4 py-2 text-xs font-bold uppercase tracking-widest hover:border-neon-pink hover:text-neon-pink"
          >
            <Trash2 className="h-3 w-3" /> Clear all
          </button>
        )}
      </div>
      {isLoading && <p className="mt-12 font-mono text-xs text-muted-foreground">Loading favorites...</p>}
      {isError && (
        <div className="mt-12">
          <FormError message="Could not load your favorites" />
        </div>
      )}
      {isEmpty && (
        <EmptyState
          accent="pink"
          icon={<Heart className="h-6 w-6" />}
          message="Tap the heart on any product to save it here for later."
        />
      )}
      {items.length > 0 && (
        <div className="mt-12 grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-3 lg:grid-cols-4 lg:gap-x-6">
          {items.map((product, index) => (
            <ProductCard key={product.id} product={product} index={index} />
          ))}
        </div>
      )}
    </div>
  )
}
