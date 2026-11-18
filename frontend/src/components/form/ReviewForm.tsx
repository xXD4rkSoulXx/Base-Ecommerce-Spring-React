import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { FieldError } from '@/components/ui/FieldError'
import { FormError } from '@/components/ui/FormError'
import { StarRating } from '@/components/ui/StarRating'
import { useCreateReview } from '@/hooks/useCreateReview'
import { reviewSchema, type ReviewFormValues } from '@/schemas/review'
import { getErrorMessage } from '@/utils/error'

interface ReviewFormProps {
  productId: string
  onClose: () => void
}

export const ReviewForm = ({ productId, onClose }: ReviewFormProps) => {
  const { mutate, isPending, error } = useCreateReview(productId)

  const {
    register,
    control,
    handleSubmit,
    formState: { errors }
  } = useForm<ReviewFormValues>({
    resolver: zodResolver(reviewSchema),
    defaultValues: { rating: 5, title: '', description: '' }
  })

  const onSubmit = (data: ReviewFormValues) =>
    mutate(data, {
      onSuccess: () => {
        toast.success('Review submitted')
        onClose()
      }
    })

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4 border border-neon-cyan/40 bg-card p-6">
      <Controller
        control={control}
        name="rating"
        render={({ field }) => (
          <div className="flex items-center gap-4">
            <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Rating</span>
            <StarRating value={field.value} onChange={field.onChange} size={26} />
            <span className="font-mono text-xs text-neon-cyan">{field.value}/5</span>
          </div>
        )}
      />
      <div>
        <input
          placeholder="Title"
          aria-label="Title"
          aria-invalid={!!errors.title}
          className="w-full border border-border bg-background px-3 py-2.5 text-sm focus:border-neon-cyan focus:outline-none"
          {...register('title')}
        />
        <FieldError message={errors.title?.message} />
      </div>
      <div>
        <textarea
          placeholder="Field report…"
          aria-label="Description"
          aria-invalid={!!errors.description}
          rows={4}
          className="w-full resize-none border border-border bg-background px-3 py-2.5 text-sm focus:border-neon-cyan focus:outline-none"
          {...register('description')}
        />
        <FieldError message={errors.description?.message} />
      </div>
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
          disabled={isPending}
          className="bg-neon-cyan px-5 py-2.5 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
        >
          {isPending ? 'Transmitting...' : 'Transmit'}
        </button>
      </div>
    </form>
  )
}
