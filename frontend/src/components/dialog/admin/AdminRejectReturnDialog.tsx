import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { useAdminRejectReturn } from '@/hooks/admin/useAdminRejectReturn'
import { rejectReturnSchema, type RejectReturnFormValues } from '@/schemas/admin/adminReturn'
import type { AdminReturnResponse } from '@/types/admin/adminReturn'
import { getErrorMessage } from '@/utils/error'

interface AdminRejectReturnDialogProps {
  orderReturn: AdminReturnResponse
  onClose: () => void
}

export const AdminRejectReturnDialog = ({ orderReturn, onClose }: AdminRejectReturnDialogProps) => {
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<RejectReturnFormValues>({
    resolver: zodResolver(rejectReturnSchema),
    defaultValues: { reason: '' }
  })

  const { mutate: rejectReturn, isPending, error } = useAdminRejectReturn()

  const onSubmit = ({ reason }: RejectReturnFormValues) =>
    rejectReturn(
      { id: orderReturn.id, reason },
      {
        onSuccess: () => {
          toast.success(`Return #${orderReturn.id} rejected`)
          onClose()
        }
      }
    )

  return (
    <Modal
      eyebrow="Reject"
      title={`Return #${orderReturn.id}`}
      description={`The customer will see this reason. ${orderReturn.customerEmail}`}
      tone="pink"
      onClose={onClose}
    >
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        {error && (
          <div className="mb-6">
            <FormError message={getErrorMessage(error, 'Could not reject the return')} />
          </div>
        )}
        <Field
          label="Reason"
          autoComplete="off"
          disabled={isPending}
          error={errors.reason?.message}
          {...register('reason')}
        />
        <div className="mt-8 flex gap-3">
          <button
            type="button"
            onClick={onClose}
            disabled={isPending}
            className="flex-1 border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-foreground disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isPending}
            className="flex-1 bg-neon-pink py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            Reject return
          </button>
        </div>
      </form>
    </Modal>
  )
}
