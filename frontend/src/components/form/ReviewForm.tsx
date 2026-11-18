import { useState, type SubmitEvent } from 'react'
import { toast } from 'sonner'
import { FormError } from '@/components/ui/FormError'
import { StarRating } from '@/components/ui/StarRating'
import { useCreateReview } from '@/hooks/useCreateReview'
import { getErrorMessage } from '@/utils/error'

interface ReviewFormProps {
  productId: string
  onClose: () => void
}

export const ReviewForm = ({ productId, onClose }: ReviewFormProps) => {
  const { mutate, isPending, error } = useCreateReview(productId)
  const [rating, setRating] = useState(5)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')

  const canSubmit = title.trim() !== '' && description.trim() !== '' && !isPending

  const handleSubmit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!canSubmit) {
      return
    }

    mutate(
      { rating, title: title.trim(), description: description.trim() },
      {
        onSuccess: () => {
          toast.success('Review submitted')
          onClose()
        }
      }
    )
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 border border-neon-cyan/40 bg-card p-6">
      <div className="flex items-center gap-4">
        <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Rating</span>
        <StarRating value={rating} onChange={setRating} size={26} />
        <span className="font-mono text-xs text-neon-cyan">{rating}/5</span>
      </div>
      <input
        value={title}
        onChange={(event) => setTitle(event.target.value)}
        placeholder="Title"
        maxLength={100}
        required
        className="w-full border border-border bg-background px-3 py-2.5 text-sm focus:border-neon-cyan focus:outline-none"
      />
      <textarea
        value={description}
        onChange={(event) => setDescription(event.target.value)}
        placeholder="Field report…"
        rows={4}
        maxLength={2000}
        required
        className="w-full resize-none border border-border bg-background px-3 py-2.5 text-sm focus:border-neon-cyan focus:outline-none"
      />
      {error && <FormError message={getErrorMessage(error, 'Could not submit your review')} />}
      <div className="flex justify-end gap-3">
        <button
          type="button"
          onClick={onClose}
          className="border border-border px-5 py-2.5 text-xs font-bold uppercase tracking-widest hover:border-foreground"
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={!canSubmit}
          className="bg-neon-cyan px-5 py-2.5 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
        >
          {isPending ? 'Transmitting...' : 'Transmit'}
        </button>
      </div>
    </form>
  )
}
