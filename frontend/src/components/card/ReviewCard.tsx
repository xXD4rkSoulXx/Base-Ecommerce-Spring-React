import { StarRating } from '@/components/ui/StarRating'
import { formatDate } from '@/utils/format'
import type { ReviewResponse } from '@/types/review'

interface ReviewCardProps {
  review: ReviewResponse
}

export const ReviewCard = ({ review }: ReviewCardProps) => (
  <article className="border border-border bg-card p-5">
    <header className="flex items-center justify-between">
      <StarRating value={review.rating} readOnly size={14} />
      <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
        {formatDate(review.createdAt)}
      </span>
    </header>
    <h3 className="mt-3 font-bold uppercase tracking-tight">{review.title}</h3>
    <p className="mt-2 text-sm text-muted-foreground">{review.description}</p>
    <p className="mt-4 font-mono text-[10px] uppercase tracking-widest text-neon-cyan">— {review.userFullName}</p>
  </article>
)
