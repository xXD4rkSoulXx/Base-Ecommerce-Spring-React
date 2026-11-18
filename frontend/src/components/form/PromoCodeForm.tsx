import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { FormError } from '@/components/ui/FormError'
import { promoCodeSchema, type PromoCodeForm as PromoCodeFormValues } from '@/schemas/checkout'

interface PromoCodeFormProps {
  onApply: (code: string) => void
  error: string | null
}

export const PromoCodeForm = ({ onApply, error }: PromoCodeFormProps) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm<PromoCodeFormValues>({
    resolver: zodResolver(promoCodeSchema),
    defaultValues: { code: '' }
  })

  const onSubmit = ({ code }: PromoCodeFormValues) => {
    onApply(code)
    reset()
  }

  const message = errors.code?.message ?? error

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate>
      <p className="mb-2 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Promo Code</p>
      <div className="flex gap-2">
        <input
          type="text"
          aria-label="Promo code"
          aria-invalid={!!errors.code}
          placeholder="Code"
          className="flex-1 border border-border bg-background px-3 py-2 font-mono text-xs uppercase tracking-widest focus:border-neon-cyan focus:outline-none"
          {...register('code')}
        />
        <button
          type="submit"
          className="bg-neon-pink px-4 py-2 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
        >
          Apply
        </button>
      </div>
      {message && (
        <div className="mt-2">
          <FormError message={message} />
        </div>
      )}
    </form>
  )
}
