import { Heart } from 'lucide-react'
import { useWishlist } from '@/hooks/useWishlist'
import type { ProductDetailsResponse } from '@/types/product'

interface ProductGallerySectionProps {
  product: ProductDetailsResponse
  images: string[]
  activeIndex: number
  onSelect: (index: number) => void
}

export const ProductGallerySection = ({ product, images, activeIndex, onSelect }: ProductGallerySectionProps) => {
  const { isInWishlist, toggleWishlist } = useWishlist()

  const isFavorite = isInWishlist(product.id)

  const handleToggleWishlist = () => {
    toggleWishlist({
      id: product.id,
      ref: product.ref,
      name: product.name,
      price: product.price,
      discountPercentage: product.discountPercentage,
      category: product.category,
      cover: images[0]
    })
  }

  return (
    <div className="relative">
      <div className="aspect-[4/5] overflow-hidden border border-border bg-card">
                <img src={images[activeIndex]} alt={product.name} fetchPriority="high" className="h-full w-full object-cover" />
      </div>
      <div className="absolute left-4 top-4 bg-background/70 px-2 py-1 font-mono text-[10px] font-bold uppercase tracking-widest backdrop-blur">
        REF: {product.ref}
      </div>
      <button
        type="button"
        onClick={handleToggleWishlist}
        aria-label={isFavorite ? 'Remove from wishlist' : 'Add to wishlist'}
        aria-pressed={isFavorite}
        className={`absolute right-4 top-4 flex h-10 w-10 items-center justify-center border backdrop-blur transition-all ${
          isFavorite
            ? 'border-neon-pink bg-neon-pink/20 text-neon-pink shadow-lg shadow-neon-pink/30'
            : 'border-border bg-background/70 text-foreground/80 hover:border-neon-pink hover:text-neon-pink'
        }`}
      >
        <Heart className="h-5 w-5" fill={isFavorite ? 'currentColor' : 'none'} strokeWidth={1.5} />
      </button>
      {images.length > 1 && (
        <div className="mt-4 grid grid-cols-5 gap-2">
          {images.map((src, index) => (
            <button
              key={src}
              type="button"
              onClick={() => onSelect(index)}
              aria-label={`View image ${index + 1}`}
              aria-pressed={index === activeIndex}
              className={`aspect-square overflow-hidden border transition-all ${
                index === activeIndex
                  ? 'border-neon-cyan ring-2 ring-neon-cyan/40'
                  : 'border-border opacity-70 hover:opacity-100'
              }`}
            >
              <img src={src} alt="" className="h-full w-full object-cover" />
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
