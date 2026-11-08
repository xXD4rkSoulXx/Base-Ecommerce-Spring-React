import { Link } from 'react-router-dom'
import { Star } from 'lucide-react'
import type { AdminReviewResponse } from '@/types/admin/adminReview'

interface AdminReviewRowProps {
  review: AdminReviewResponse
  canEdit: boolean
  canViewProduct: boolean
  isBusy: boolean
  onToggle: (review: AdminReviewResponse) => void
}

const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium' })

export const AdminReviewRow = ({ review, canEdit, canViewProduct, isBusy, onToggle }: AdminReviewRowProps) => (
  <tr className={`border-b border-border align-top last:border-b-0 ${review.hidden ? 'opacity-60' : ''}`}>
    <td className="p-4">
      {canViewProduct ? (
        <Link to={`/admin/products/${review.productId}`} className="font-bold hover:text-neon-cyan">
          {review.productName}
        </Link>
      ) : (
        <p className="font-bold">{review.productName}</p>
      )}
      <p className="font-mono text-xs text-muted-foreground">{review.productRef}</p>
    </td>
    <td className="p-4">
      <p className="text-sm">{review.customerName}</p>
      <p className="font-mono text-xs text-muted-foreground">{review.customerEmail}</p>
    </td>
    <td className="p-4">
      <p className="flex items-center gap-1 font-mono text-sm text-neon-cyan">
        <Star className="h-3.5 w-3.5" /> {review.rating}/5
      </p>
      <p className="mt-1 text-sm font-bold">{review.title}</p>
      <p className="mt-1 line-clamp-3 max-w-md text-xs text-muted-foreground">{review.description}</p>
    </td>
    <td className="p-4">
      <span
        className={`inline-block border px-2 py-0.5 font-mono text-[10px] uppercase tracking-widest ${
          review.hidden ? 'border-neon-pink text-neon-pink' : 'border-neon-cyan text-neon-cyan'
        }`}
      >
        {review.hidden ? 'Hidden' : 'Visible'}
      </span>
    </td>
    <td className="p-4 font-mono text-xs text-muted-foreground">
      {dateFormatter.format(new Date(review.createdAt))}
    </td>
    <td className="p-4 text-right">
      {canEdit && (
        <button
          onClick={() => onToggle(review)}
          disabled={isBusy}
          className={`text-xs font-bold uppercase tracking-widest disabled:opacity-50 ${
            review.hidden ? 'hover:text-neon-cyan' : 'text-neon-pink hover:opacity-80'
          }`}
        >
          {review.hidden ? 'Show' : 'Hide'}
        </button>
      )}
    </td>
  </tr>
)
