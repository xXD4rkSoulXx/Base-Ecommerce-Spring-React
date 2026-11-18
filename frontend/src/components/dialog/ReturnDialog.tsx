import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { Package, RotateCcw } from 'lucide-react'
import { toast } from 'sonner'
import { ReturnItemRow } from '@/components/row/ReturnItemRow'
import { FieldError } from '@/components/ui/FieldError'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { SectionTitle } from '@/components/ui/SectionTitle'
import { RETURN_REASON_LABELS } from '@/constants'
import { useRequestReturn } from '@/hooks/useRequestReturn'
import { returnSchema, type ReturnFormValues } from '@/schemas/return'
import type { OrderDetailsResponse, ReturnReason } from '@/types/order'
import { getErrorMessage } from '@/utils/error'

interface ReturnDialogProps {
  order: OrderDetailsResponse
  onClose: () => void
}

const RETURN_REASONS = Object.entries(RETURN_REASON_LABELS) as [ReturnReason, string][]

export const ReturnDialog = ({ order, onClose }: ReturnDialogProps) => {
  const { mutate, isPending, error } = useRequestReturn(order.id)

  const {
    register,
    control,
    handleSubmit,
    formState: { errors }
  } = useForm<ReturnFormValues>({
    resolver: zodResolver(returnSchema),
    defaultValues: { quantities: {}, comment: '' }
  })

  const reason = useWatch({ control, name: 'reason' })
  const returnableItems = order.items.filter((item) => item.returnableQuantity > 0)
  const quantitiesError = (errors.quantities as { message?: string } | undefined)?.message

  const onSubmit = ({ quantities, reason, comment }: ReturnFormValues) =>
    mutate(
      {
        reason,
        comment: comment || undefined,
        items: Object.entries(quantities)
          .filter(([, quantity]) => quantity > 0)
          .map(([orderItemId, quantity]) => ({ orderItemId: Number(orderItemId), quantity }))
      },
      {
        onSuccess: () => {
          toast.success('Return requested')
          onClose()
        }
      }
    )

  return (
    <Modal eyebrow={`Order #${order.id}`} title="Return items" size="lg" onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-6">
        <div>
          <SectionTitle as="h3" icon={<Package className="h-3.5 w-3.5" />}>
            Select items
          </SectionTitle>
          <Controller
            control={control}
            name="quantities"
            render={({ field }) => (
              <div className="border border-border">
                {returnableItems.map((item) => (
                  <ReturnItemRow
                    key={item.id}
                    item={item}
                    quantity={field.value[item.id] ?? 0}
                    onChange={(quantity) => field.onChange({ ...field.value, [item.id]: quantity })}
                  />
                ))}
              </div>
            )}
          />
          <FieldError message={quantitiesError} />
        </div>
        <div className="space-y-3">
          <h3 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Reason</h3>
          <div>
            <select
              aria-label="Reason"
              aria-invalid={!!errors.reason}
              className="w-full border border-border bg-background px-3 py-2 font-mono text-xs"
              {...register('reason')}
            >
              <option value="">Select a reason</option>
              {RETURN_REASONS.map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
            <FieldError message={errors.reason?.message} />
          </div>
          <div>
            <textarea
              aria-label="Comment"
              aria-invalid={!!errors.comment}
              rows={3}
              placeholder={reason === 'OTHER' ? 'Please describe the reason (required)' : 'Comment (optional)'}
              className="w-full resize-none border border-border bg-background px-3 py-2 font-mono text-xs"
              {...register('comment')}
            />
            <FieldError message={errors.comment?.message} />
          </div>
        </div>
        <p className="font-mono text-[10px] leading-relaxed text-muted-foreground">
          Return shipping is free. You will be refunded to your original payment method, and the shipping cost is
          refunded in proportion to the weight of the items you return.
        </p>
        {error && <FormError message={getErrorMessage(error, 'Failed to request return')} />}
        <button
          type="submit"
          disabled={isPending}
          className="flex w-full items-center justify-center gap-2 border border-neon-cyan bg-neon-cyan/10 py-2.5 font-mono text-xs uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan/20 disabled:opacity-40"
        >
          <RotateCcw className="h-3.5 w-3.5" />
          {isPending ? 'Requesting...' : 'Request return'}
        </button>
      </form>
    </Modal>
  )
}
