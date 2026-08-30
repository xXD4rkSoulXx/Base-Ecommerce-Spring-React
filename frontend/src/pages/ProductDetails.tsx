import { useEffect, useMemo, useRef, useState, type SubmitEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, MessageSquarePlus, Star } from 'lucide-react'
import { Loading } from '@/pages/Loading'
import { ErrorPage } from '@/pages/ErrorPage'
import { ReviewLockedNotice } from '@/components/ui/ReviewLockedNotice'
import { StarRating } from '@/components/ui/StarRating'
import { useProductDetails } from '@/hooks/useProductDetails'
import { useRelatedProducts } from '@/hooks/useRelatedProducts'
import { useInfiniteProductReviews } from '@/hooks/useInfiniteProductReviews'
import { useReviewEligibility } from '@/hooks/useReviewEligibility'
import { useCreateReview } from '@/hooks/useCreateReview'
import { useCartStore } from '@/store/useCartStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { useAuthStore } from '@/store/useAuthStore'
import type { ProductResponse } from '@/types/product'

export const ProductDetails = () => {
  const navigate = useNavigate()
  const { data: product, isLoading, isError } = useProductDetails()
  const { data: relatedProducts } = useRelatedProducts()
  const addToCart = useCartStore((state) => state.addToCart)
  const { items: wishlistItems, addToWishlist, removeFromWishlist } = useWishlistStore()

  const user = useAuthStore((state) => state.user)
  const isUserAuthenticated = !!user

  const [activeColor, setActiveColor] = useState<string>()
  const [activeSize, setActiveSize] = useState<string>('')
  const [activeImgIndex, setActiveImgIndex] = useState<number>(0)

  const [isOpenReviewForm, setIsOpenReviewForm] = useState(false)
  const [newRating, setNewRating] = useState(5)
  const [reviewTitle, setReviewTitle] = useState('')
  const [reviewDescription, setReviewDescription] = useState('')

  const {
    data: reviewsData,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    status: reviewsStatus
  } = useInfiniteProductReviews()

  const { data: reviewEligibility } = useReviewEligibility()
  const createReviewMutation = useCreateReview()

  const isFavorite = useMemo(() => {
    return product ? wishlistItems.some((item) => item.id === product.id) : false
  }, [product, wishlistItems])

  const availableColors = useMemo(() => {
    if (!product?.productSkus) {
      return []
    }

    return Array.from(new Set(product.productSkus.map((sku) => sku.color)))
  }, [product])

  const filteredSkusByColor = useMemo(() => {
    if (!product?.productSkus || !activeColor) {
      return []
    }

    return product.productSkus.filter((sku) => sku.color === activeColor)
  }, [product, activeColor])

  const selectedSku = useMemo(() => {
    if (!product?.productSkus) {
      return null
    }

    return product.productSkus.find(
      (sku) => sku.color === activeColor && sku.size === activeSize
    ) || null
  }, [product, activeColor, activeSize])

  const reviewsObserverTarget = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    const target = reviewsObserverTarget.current

    if (!target) {
      return
    }

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage()
        }
      },
      { threshold: 0.1 }
    )

    observer.observe(target)

    return () => { observer.disconnect() }
  }, [hasNextPage, isFetchingNextPage, fetchNextPage])

  if (isLoading) {
    return <Loading />
  }
  
  if (isError || !product) {
    return <ErrorPage />
  }

  if (product.productSkus && product.productSkus.length > 0 && !activeColor) {
    setActiveColor(product.productSkus[0].color)
  }

  const reviewsList = reviewsData?.pages.flatMap((page) => page.content) || []

  const productFirstImg = product.productSkus?.find(sku => sku.paths && sku.paths.length > 0)?.paths[0] || ''

  const skuWithImages = activeColor && product?.productSkus
    ? product.productSkus.find((sku) => sku.color === activeColor && sku.paths && sku.paths.length > 0)
    : null

  const activeImageSource = selectedSku && selectedSku.paths && selectedSku.paths.length > 0
    ? selectedSku
    : skuWithImages

  const galleryImages = activeImageSource && activeImageSource.paths.length > 0
    ? activeImageSource.paths
    : [productFirstImg]

  const currentImg = galleryImages[activeImgIndex] || productFirstImg

  const effectiveDiscountPercentage = selectedSku && selectedSku.discountPercentage > 0
    ? selectedSku.discountPercentage
    : product.discountPercentage

  const hasDiscount = effectiveDiscountPercentage > 0
  const finalPrice = hasDiscount
    ? (product.price * (1 - effectiveDiscountPercentage / 100)).toFixed(2)
    : product.price.toFixed(2)
  
  const handleToggleWishlist = (e: React.MouseEvent) => {
    e.stopPropagation()
    
    if (!product) {
      return
    }
    
    const productDto: ProductResponse = {
      id: product.id,
      ref: product.ref,
      name: product.name,
      price: Number(product.price),
      discountPercentage: effectiveDiscountPercentage,
      category: product.category,
      cover: productFirstImg || galleryImages[0] || ''
    }

    if (isFavorite) {
      removeFromWishlist(product.id)
    } else {
      addToWishlist(productDto)
    }
  }

  const handleSubmitReview = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (!reviewTitle || !reviewDescription) {
      return
    }

    createReviewMutation.mutate(
      { rating: newRating, title: reviewTitle, description: reviewDescription },
      {
        onSuccess: () => {
          setIsOpenReviewForm(false)
          setReviewTitle('')
          setReviewDescription('')
          setNewRating(5)
        }
      }
    )
  }

  const handleAddToCart = () => {
    const firstSkuWithImage = product.productSkus?.find(sku => sku.paths && sku.paths.length > 0)
    const fallbackImg = firstSkuWithImage && firstSkuWithImage.paths.length > 0 
      ? firstSkuWithImage.paths[0] 
      : ''

    const productDto: ProductResponse = {
      id: product.id,
      ref: product.ref,
      name: product.name,
      price: Number(product.price),
      discountPercentage: effectiveDiscountPercentage,
      category: product.category,
      cover: fallbackImg
    }

    addToCart(productDto)
  }

  const handleSelectColor = (color: string) => {
    setActiveColor(color)
    setActiveSize('')
    setActiveImgIndex(0)
  }

  const handleSelectSize = (size: string) => {
    setActiveSize(size)
    setActiveImgIndex(0)
  }

  const handleClickRelatedProduct = (productId: number) => {
    navigate(`/product/${productId}`)
    window.scrollTo(0, 0)
  }

  return (
    <>
      <div className="mx-auto max-w-[1440px] px-6 py-12 md:px-10">
        <button
          onClick={() => navigate(-1)}
          className="mb-8 flex items-center gap-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
        >
          <ArrowLeft className="h-3 w-3" /> Back
        </button>
        <div className="grid gap-12 lg:grid-cols-2">
          <div className="relative">
            <div className="aspect-[4/5] overflow-hidden border border-border bg-card">
              <img
                src={currentImg}
                alt={product.name}
                className="h-full w-full object-cover"
              />
            </div>
            <div className="absolute left-4 top-4 bg-background/70 px-2 py-1 font-mono text-[10px] font-bold uppercase tracking-widest backdrop-blur">
              REF: {product.ref}
            </div>
            <div className="absolute right-4 top-4">
              <button
                type="button"
                onClick={handleToggleWishlist}
                aria-label={isFavorite ? 'Remove from favorites' : 'Add to favorites'}
                aria-pressed={isFavorite}
                className={`flex h-10 w-10 items-center justify-center border backdrop-blur transition-all ${
                  isFavorite
                    ? 'border-neon-pink bg-neon-pink/20 text-neon-pink shadow-lg shadow-neon-pink/30'
                    : 'border-border bg-background/70 text-foreground/80 hover:border-neon-pink hover:text-neon-pink'
                }`}
              >
                <svg
                  className="h-5 w-5"
                  fill={isFavorite ? 'currentColor' : 'none'}
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  strokeWidth={1.5}
                >
                  <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z" />
                </svg>
              </button>
            </div>
            {galleryImages.length > 1 && (
              <div className="mt-4 grid grid-cols-5 gap-2">
                {galleryImages.map((src, i) => (
                  <button
                    key={src + i}
                    onClick={() => setActiveImgIndex(i)}
                    aria-label={`View image ${i + 1}`}
                    className={`aspect-square overflow-hidden border transition-all ${
                      i === activeImgIndex
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
          <div className="flex flex-col">
            <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-neon-pink">
              {product.category}
            </p>
            <h1 className="mt-4 font-display text-5xl uppercase leading-none tracking-tighter md:text-6xl">
              {product.name}
            </h1>
            <a href="#reviews" className="mt-4 flex items-center gap-2 text-xs">
              <Star className="h-3.5 w-3.5 fill-neon-cyan text-neon-cyan" strokeWidth={1.5} />
              <span className="font-mono text-neon-cyan">{product.averageRating.toFixed(1)}</span>
              <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                / {product.totalReviews} review{product.totalReviews === 1 ? '' : 's'}
              </span>
            </a>
            <div className="mt-6 flex flex-wrap items-baseline gap-4">
              {hasDiscount ? (
                <>
                  <span className="font-mono text-3xl text-neon-pink">${finalPrice}</span>
                  <span className="font-mono text-lg text-muted-foreground line-through">
                    ${product.price}
                  </span>
                  <span className="bg-neon-pink px-2 py-1 font-mono text-[10px] font-extrabold uppercase tracking-widest text-primary-foreground">
                    -{effectiveDiscountPercentage}% off
                  </span>
                </>
              ) : (
                <span className="font-mono text-3xl text-neon-cyan">${product.price}</span>
              )}
              <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                {selectedSku
                  ? `Stock: ${selectedSku.stock} units · ID: ${selectedSku.id}`
                  : `Stock: ${product.productSkus.reduce((s, v) => s + v.stock, 0)} units total`}
              </span>
            </div>
            <p className="mt-8 leading-relaxed text-muted-foreground">
              {product.description}
            </p>
            <div className="mt-10">
              <h3 className="mb-3 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                Color: <span className="text-foreground">{activeColor || '—'}</span>
              </h3>
              <div className="flex flex-wrap gap-3">
                {availableColors.map((c) => {
                  const isSel = activeColor === c
                  const colorStock = product.productSkus
                    .filter((v) => v.color === c)
                    .reduce((s, v) => s + v.stock, 0)

                  return (
                    <button
                      key={c}
                      type="button"
                      onClick={() => handleSelectColor(c)}
                      disabled={colorStock === 0}
                      title={`${c} · ${colorStock} units`}
                      aria-label={c}
                      aria-pressed={isSel}
                      className={`relative h-10 w-10 border transition-all ${
                        colorStock === 0 ? 'opacity-30 cursor-not-allowed line-through' : ''
                      } ${
                        isSel
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
              <h3 className="mb-3 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                Size {activeColor && <span className="text-foreground">/ {activeColor}</span>}
              </h3>
              <div className="flex flex-wrap gap-2">
                {filteredSkusByColor.map((sku) => {
                  const out = sku.stock === 0
                  const displaySize = sku.size.startsWith('_') ? sku.size.substring(1) : sku.size

                  return (
                    <button
                      key={sku.id}
                      onClick={() => handleSelectSize(sku.size)}
                      disabled={out}
                      title={out ? 'Out of stock' : `${sku.stock} units`}
                      className={`min-w-[3.5rem] border px-4 py-3 text-xs font-bold uppercase tracking-widest transition-all ${
                        out
                          ? 'cursor-not-allowed border-border text-muted-foreground line-through opacity-40'
                          : activeSize === sku.size
                            ? 'border-neon-cyan bg-neon-cyan text-primary-foreground'
                            : 'border-border hover:border-foreground'
                      }`}
                    >
                      {displaySize}
                    </button>
                  )
                })}
                {filteredSkusByColor.length === 0 && (
                  <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                    No variants for this color
                  </p>
                )}
              </div>
            </div>
            <div className="mt-12 flex flex-col gap-3 sm:flex-row">
              <button
                onClick={handleAddToCart}
                className="flex-1 bg-neon-cyan px-8 py-5 text-sm font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80"
              >
                Add to Cart
              </button>
            </div>
          </div>
        </div>
      </div>
      {relatedProducts && relatedProducts.length > 0 && (
        <section className="mx-auto max-w-[1440px] border-t border-border px-6 py-20 md:px-10">
          <div className="mb-10 flex items-end justify-between">
            <h2 className="font-display text-3xl uppercase italic tracking-tighter md:text-5xl">
              Related <span className="text-neon-cyan">Objects</span>
            </h2>
          </div>
          <div className="grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-4 lg:gap-x-6">
            {relatedProducts.map((p) => (
              <div 
                key={p.id} 
                className="group border border-border bg-card p-4 cursor-pointer transition-colors hover:border-neon-cyan"
                onClick={() => handleClickRelatedProduct(p.id)}
              >
                <div className="aspect-[4/5] overflow-hidden bg-background mb-4 border border-border">
                  <img 
                    src={p.cover} 
                    alt={p.name} 
                    className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105" 
                  />
                </div>
                <h3 className="font-bold text-sm uppercase truncate">{p.name}</h3>
                <p className="font-mono text-xs text-neon-cyan mt-1">${p.price.toFixed(2)}</p>
              </div>
            ))}
          </div>
        </section>
      )}
      <div id="reviews">
        <section id="reviews" className="mx-auto max-w-[1440px] border-t border-border px-6 py-16 md:px-10">
          <div className="flex flex-wrap items-end justify-between gap-6">
            <div>
              <h2 className="mt-3 font-display text-3xl uppercase italic tracking-tighter md:text-5xl">
                Reviews <span className="text-neon-cyan">({product.totalReviews})</span>
              </h2>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-right">
                <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Signal Score</p>
                <p className="font-display text-4xl text-neon-cyan">
                  {product.averageRating.toFixed(1)}
                  <span className="ml-1 text-base text-muted-foreground">/5</span>
                </p>
              </div>
              <StarRating value={product.averageRating} readOnly size={22} />
            </div>
          </div>
          <div className="mt-10">
            {!isUserAuthenticated ? (
              <ReviewLockedNotice message="Only logged in buyers with delivered orders can transmit a review." />
            ) : reviewEligibility?.status === 'NOT_PURCHASED' ? (
              <ReviewLockedNotice message="You need to have purchased and received this product before you can review it." />
            ) : reviewEligibility?.status === 'ALREADY_REVIEWED' ? (
              <ReviewLockedNotice message="You've already reviewed this product. Only 1 review per product is allowed." />
            ) : !isOpenReviewForm ? (
              <button
                onClick={() => setIsOpenReviewForm(true)}
                className="flex items-center gap-2 border border-neon-cyan bg-neon-cyan/10 px-5 py-3 text-xs font-black uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan hover:text-primary-foreground"
              >
                <MessageSquarePlus className="h-4 w-4" /> Write a Review
              </button>
            ) : (
              <form onSubmit={handleSubmitReview} className="space-y-4 border border-neon-cyan/40 bg-card p-6">
                <div className="flex items-center gap-4">
                  <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Rating</span>
                  <StarRating value={newRating} onChange={setNewRating} size={26} />
                  <span className="font-mono text-xs text-neon-cyan">{newRating}/5</span>
                </div>
                <input
                  value={reviewTitle}
                  onChange={(e) => setReviewTitle(e.target.value)}
                  placeholder="Title"
                  required
                  className="w-full border border-border bg-background px-3 py-2.5 text-sm focus:border-neon-cyan focus:outline-none"
                />
                <textarea
                  value={reviewDescription}
                  onChange={(e) => setReviewDescription(e.target.value)}
                  placeholder="Field report…"
                  rows={4}
                  required
                  className="w-full resize-none border border-border bg-background px-3 py-2.5 text-sm focus:border-neon-cyan focus:outline-none"
                />
                <div className="flex justify-end gap-3">
                  <button
                    type="button"
                    onClick={() => setIsOpenReviewForm(false)}
                    className="border border-border px-5 py-2.5 text-xs font-bold uppercase tracking-widest hover:border-foreground"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={createReviewMutation.isPending}
                    className="bg-neon-cyan px-5 py-2.5 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
                  >
                    {createReviewMutation.isPending ? "Transmitting..." : "Transmit"}
                  </button>
                </div>
              </form>
            )}
          </div>
          <div className="mt-10 grid gap-4 md:grid-cols-2">
            {reviewsStatus === "pending" ? (
              <p className="font-mono text-xs text-muted-foreground">Loading transmissions...</p>
            ) : reviewsList.length === 0 ? (
              <p className="col-span-full border border-dashed border-border p-8 text-center font-mono text-xs uppercase tracking-widest text-muted-foreground">
                No signals yet. Be first.
              </p>
            ) : (
              reviewsList.map((review) => (
                <article key={review.id} className="border border-border bg-card p-5">
                  <header className="flex items-center justify-between">
                    <StarRating value={review.rating} readOnly size={14} />
                    <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                      {review.createdAt ? new Date(review.createdAt).toLocaleDateString() : ''}
                    </span>
                  </header>
                  <h3 className="mt-3 font-bold uppercase tracking-tight">{review.title}</h3>
                  <p className="mt-2 text-sm text-muted-foreground">{review.description}</p>
                  <p className="mt-4 font-mono text-[10px] uppercase tracking-widest text-neon-cyan">
                    — {review.userFullName}
                  </p>
                </article>
              ))
            )}
          </div>
          <div ref={reviewsObserverTarget} className="mt-8 flex h-16 items-center justify-center font-mono text-xs uppercase tracking-widest text-neon-cyan">
            {isFetchingNextPage && <span className="animate-pulse">Loading more signals...</span>}
            {!hasNextPage && reviewsList.length > 0 && (
              <span className="text-muted-foreground">— End of transmissions —</span>
            )}
          </div>
        </section>
      </div>
    </>
  )
}
