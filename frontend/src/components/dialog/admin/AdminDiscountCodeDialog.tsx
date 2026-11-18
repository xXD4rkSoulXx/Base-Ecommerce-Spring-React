import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { ReadOnlyField } from '@/components/ui/ReadOnlyField'
import { useAdminCreateDiscountCode } from '@/hooks/admin/useAdminCreateDiscountCode'
import { useAdminUpdateDiscountCode } from '@/hooks/admin/useAdminUpdateDiscountCode'
import { discountCodeSchema, type DiscountCodeFormValues } from '@/schemas/admin/adminDiscountCode'
import type { AdminDiscountCodeResponse } from '@/types/admin/adminDiscountCode'
import { getErrorMessage } from '@/utils/error'

interface AdminDiscountCodeDialogProps {
  discountCode: AdminDiscountCodeResponse | null
  onClose: () => void
}

export const AdminDiscountCodeDialog = ({ discountCode, onClose }: AdminDiscountCodeDialogProps) => {
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<DiscountCodeFormValues>({
    resolver: zodResolver(discountCodeSchema),
    defaultValues: { code: discountCode?.code ?? '', discountPercentage: discountCode?.discountPercentage }
  })

  const { mutate: createCode, isPending: isCreating, error: createError } = useAdminCreateDiscountCode()
  const { mutate: updateCode, isPending: isUpdating, error: updateError } = useAdminUpdateDiscountCode()
  const isPending = isCreating || isUpdating
  const error = createError ?? updateError

  const onSubmit = ({ code, discountPercentage }: DiscountCodeFormValues) => {
    const callbacks = {
      onSuccess: () => {
        toast.success(discountCode ? 'Code updated' : 'Code created')
        onClose()
      }
    }

    if (discountCode) {
      updateCode({ id: discountCode.id, request: { discountPercentage } }, callbacks)

      return
    }

    createCode({ code, discountPercentage }, callbacks)
  }

  return (
    <Modal eyebrow="Discount code" title={discountCode ? 'Edit code' : 'New code'} onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="space-y-4">
          {discountCode ? (
            <ReadOnlyField label="Code" value={discountCode.code} />
          ) : (
            <Field
              label="Code"
              accent="cyan"
              autoComplete="off"
              disabled={isPending}
              error={errors.code?.message}
              {...register('code')}
            />
          )}
          <Field
            label="Discount (%)"
            type="number"
            accent="cyan"
            autoComplete="off"
            disabled={isPending}
            error={errors.discountPercentage?.message}
            {...register('discountPercentage', { valueAsNumber: true })}
          />
        </div>
        <p className="mt-3 text-xs text-muted-foreground">
          The code cannot be changed after it is created. Changing the discount only affects future purchases.
        </p>
        {error && (
          <div className="mt-4">
            <FormError message={getErrorMessage(error, 'Could not save the code')} />
          </div>
        )}
        <div className="mt-6 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="border border-border px-5 py-3 text-xs font-black uppercase tracking-widest hover:border-foreground/40"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isPending}
            className="bg-neon-cyan px-5 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            Save
          </button>
        </div>
      </form>
    </Modal>
  )
}
