import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, Star } from 'lucide-react'
import { toast } from 'sonner'
import { ProductGallerySection } from '@/components/section/ProductGallerySection'
import { useCart } from '@/hooks/useCart'
import type { GuestCartItem } from '@/store/useCartStore'
import type { ProductColor, ProductDetailsResponse, ProductSize } from '@/types/product'
import { formatPrice, formatSize, pluralize } from '@/utils/format'
import { getDiscountedPrice } from '@/utils/price'

interface ProductOverviewSectionProps {
  product: ProductDetailsResponse
}

export const ProductOverviewSection = ({ product }: ProductOverviewSectionProps) => {
  const navigate = useNavigate()
  const { addToCart } = useCart()
  const [color, setColor] = useState<ProductColor | undefined>(product.productSkus[0]?.color)
  const [size, setSize] = useState<ProductSize>()
  const [imageIndex, setImageIndex] = useState(0)

  const colors = [...new Set(product.productSkus.map((sku) => sku.color))]
  const colorSkus = product.productSkus.filter((sku) => sku.color === color)
  const selectedSku = colorSkus.find((sku) => sku.size === size)

  const fallbackImage = product.productSkus.find((sku) => sku.paths.length > 0)?.paths[0] ?? ''
  const gallerySku = selectedSku?.paths.length ? selectedSku : colorSkus.find((sku) => sku.paths.length > 0)
  const images = gallerySku?.paths ?? [fallbackImage]

  const discountPercentage =
    selectedSku && selectedSku.discountPercentage > 0 ? selectedSku.discountPercentage : product.discountPercentage
  const hasDiscount = discountPercentage > 0
  const totalStock = product.productSkus.reduce((total, sku) => total + sku.stock, 0)

  const canAddToCart = !!selectedSku && selectedSku.stock > 0
  const addToCartLabel = !selectedSku ? 'Select a Size' : canAddToCart ? 'Add to Cart' : 'Out of Stock'

  const handleSelectColor = (newColor: ProductColor) => {
    setColor(newColor)
    setSize(undefined)
    setImageIndex(0)
  }

  const handleSelectSize = (newSize: ProductSize) => {
    setSize(newSize)
    setImageIndex(0)
  }

  const handleAddToCart = () => {
    if (!selectedSku) {
      return
    }

    const item: GuestCartItem = {
      productSkuId: selectedSku.id,
      productId: product.id,
      productName: product.name,
      productRef: product.ref,
      size: selectedSku.size,
      color: selectedSku.color,
      cover: images[0],
      price: product.price,
      discountPercentage,
      stock: selectedSku.stock,
      quantity: 1,
      available: true
    }

    addToCart(item, () => toast.success(`${product.name} added to cart`))
  }

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-12 md:px-10">
      <button
        type="button"
        onClick={() => navigate(-1)}
        className="mb-8 flex items-center gap-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
      >
        <ArrowLeft className="h-3 w-3" /> Back
      </button>
      <div className="grid gap-12 lg:grid-cols-2">
        <ProductGallerySection
          product={product}
          images={images}
          activeIndex={imageIndex}
          onSelect={setImageIndex}
        />
        <div className="flex flex-col">
          <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-neon-pink">{product.category}</p>
          <h1 className="mt-4 font-display text-5xl uppercase leading-none tracking-tighter md:text-6xl">
            {product.name}
          </h1>
          <a href="#reviews" className="mt-4 flex items-center gap-2 text-xs">
            <Star className="h-3.5 w-3.5 fill-neon-cyan text-neon-cyan" strokeWidth={1.5} />
            <span className="font-mono text-neon-cyan">{product.averageRating.toFixed(1)}</span>
            <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              / {pluralize(product.totalReviews, 'review')}
            </span>
          </a>
          <div className="mt-6 flex flex-wrap items-baseline gap-4">
            {hasDiscount ? (
              <>
                <span className="font-mono text-3xl text-neon-pink">
                  {formatPrice(getDiscountedPrice(product.price, discountPercentage))}
                </span>
                <span className="font-mono text-lg text-muted-foreground line-through">
                  {formatPrice(product.price)}
                </span>
                <span className="bg-neon-pink px-2 py-1 font-mono text-[10px] font-extrabold uppercase tracking-widest text-primary-foreground">
                  -{discountPercentage}% off
                </span>
              </>
            ) : (
              <span className="font-mono text-3xl text-neon-cyan">{formatPrice(product.price)}</span>
            )}
            <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              {selectedSku ? `Stock: ${selectedSku.stock} units` : `Stock: ${totalStock} units total`}
            </span>
          </div>
          <p className="mt-8 leading-relaxed text-muted-foreground">{product.description}</p>
          <div className="mt-10">
            <h2 className="mb-3 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
              Color: <span className="text-foreground">{color ?? '—'}</span>
            </h2>
            <div className="flex flex-wrap gap-3">
              {colors.map((c) => {
                const colorStock = product.productSkus
                  .filter((sku) => sku.color === c)
                  .reduce((total, sku) => total + sku.stock, 0)

                return (
                  <button
                    key={c}
                    type="button"
                    onClick={() => handleSelectColor(c)}
                    disabled={colorStock === 0}
                    title={`${c} · ${colorStock} units`}
                    aria-label={c}
                    aria-pressed={color === c}
                    className={`h-10 w-10 border transition-all disabled:cursor-not-allowed disabled:opacity-30 ${
                      color === c
                        ? 'border-neon-cyan ring-2 ring-neon-cyan/40 ring-offset-2 ring-offset-background'
                        : 'border-border hover:border-foreground'
                    }`}
                    style={{ backgroundColor: c.toLowerCase() }}
                  />
                )
              })}
            </div>
          </div>
          <div className="mt-8">
            <h2 className="mb-3 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
              Size {color && <span className="text-foreground">/ {color}</span>}
            </h2>
            <div className="flex flex-wrap gap-2">
              {colorSkus.map((sku) => {
                const isOut = sku.stock === 0

                return (
                  <button
                    key={sku.id}
                    type="button"
                    onClick={() => handleSelectSize(sku.size)}
                    disabled={isOut}
                    aria-pressed={size === sku.size}
                    title={isOut ? 'Out of stock' : `${sku.stock} units`}
                    className={`min-w-[3.5rem] border px-4 py-3 text-xs font-bold uppercase tracking-widest transition-all disabled:cursor-not-allowed disabled:text-muted-foreground disabled:line-through disabled:opacity-40 ${
                      size === sku.size
                        ? 'border-neon-cyan bg-neon-cyan text-primary-foreground'
                        : 'border-border hover:border-foreground'
                    }`}
                  >
                    {formatSize(sku.size)}
                  </button>
                )
              })}
              {colorSkus.length === 0 && (
                <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                  No variants for this color
                </p>
              )}
            </div>
          </div>
          <button
            type="button"
            onClick={handleAddToCart}
            disabled={!canAddToCart}
            className="mt-12 bg-neon-cyan px-8 py-5 text-sm font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80 disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:opacity-40"
          >
            {addToCartLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
