import { useState } from 'react'
import { MessageSquarePlus } from 'lucide-react'
import { ReviewCard } from '@/components/card/ReviewCard'
import { ReviewForm } from '@/components/form/ReviewForm'
import { ReviewLockedNotice } from '@/components/ui/ReviewLockedNotice'
import { StarRating } from '@/components/ui/StarRating'
import { useInfiniteProductReviews } from '@/hooks/useInfiniteProductReviews'
import { useInfiniteScroll } from '@/hooks/useInfiniteScroll'
import { useReviewEligibility } from '@/hooks/useReviewEligibility'
import { useAuthStore } from '@/store/useAuthStore'
import type { ProductDetailsResponse } from '@/types/product'
import type { ReviewLockReason } from '@/types/review'

interface ReviewsSectionProps {
  product: ProductDetailsResponse
}

const REVIEW_LOCKED_MESSAGES: Record<ReviewLockReason, string> = {
  GUEST: 'Only logged in buyers with delivered orders can transmit a review.',
  NOT_PURCHASED: 'You need to have purchased and received this product before you can review it.',
  ALREADY_REVIEWED: "You've already reviewed this product. Only 1 review per product is allowed."
}

export const ReviewsSection = ({ product }: ReviewsSectionProps) => {
  const id = String(product.id)
  const isAuthenticated = useAuthStore((state) => !!state.user)
  const { data: eligibility } = useReviewEligibility(id)
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isPending } = useInfiniteProductReviews(id)
  const observerTarget = useInfiniteScroll({ hasNextPage, isFetchingNextPage, fetchNextPage })
  const [isFormOpen, setIsFormOpen] = useState(false)

  const status = isAuthenticated ? eligibility?.status : 'GUEST'
  const reviews = data?.pages.flatMap((page) => page.content) ?? []

  return (
    <section id="reviews" className="mx-auto max-w-[1440px] border-t border-border px-6 py-16 md:px-10">
      <div className="flex flex-wrap items-end justify-between gap-6">
        <h2 className="font-display text-3xl uppercase italic tracking-tighter md:text-5xl">
          Reviews <span className="text-neon-cyan">({product.totalReviews})</span>
        </h2>
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
        {status === 'ELIGIBLE' && !isFormOpen && (
          <button
            type="button"
            onClick={() => setIsFormOpen(true)}
            className="flex items-center gap-2 border border-neon-cyan bg-neon-cyan/10 px-5 py-3 text-xs font-black uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan hover:text-primary-foreground"
          >
            <MessageSquarePlus className="h-4 w-4" /> Write a Review
          </button>
        )}
        {status === 'ELIGIBLE' && isFormOpen && <ReviewForm productId={id} onClose={() => setIsFormOpen(false)} />}
        {status && status !== 'ELIGIBLE' && <ReviewLockedNotice message={REVIEW_LOCKED_MESSAGES[status]} />}
      </div>
      <div className="mt-10 grid gap-4 md:grid-cols-2">
        {isPending && <p className="font-mono text-xs text-muted-foreground">Loading transmissions...</p>}
        {!isPending && reviews.length === 0 && (
          <p className="col-span-full border border-dashed border-border p-8 text-center font-mono text-xs uppercase tracking-widest text-muted-foreground">
            No signals yet. Be first.
          </p>
        )}
        {reviews.map((review) => (
          <ReviewCard key={review.id} review={review} />
        ))}
      </div>
      <div
        ref={observerTarget}
        className="mt-8 flex h-16 items-center justify-center font-mono text-xs uppercase tracking-widest text-neon-cyan"
      >
        {isFetchingNextPage && <span className="animate-pulse">Loading more signals...</span>}
        {!hasNextPage && reviews.length > 0 && <span className="text-muted-foreground">— End of transmissions —</span>}
      </div>
    </section>
  )
}
