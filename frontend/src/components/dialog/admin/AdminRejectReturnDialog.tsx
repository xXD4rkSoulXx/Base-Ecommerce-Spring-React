import { useState, type SubmitEvent } from 'react'
import { toast } from 'sonner'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { Modal } from '@/components/ui/Modal'
import { useAdminRejectReturn } from '@/hooks/admin/useAdminRejectReturn'
import type { AdminReturnResponse } from '@/types/admin/adminReturn'
import { getErrorMessage } from '@/utils/error'

interface AdminRejectReturnDialogProps {
  orderReturn: AdminReturnResponse
  onClose: () => void
}

export const AdminRejectReturnDialog = ({ orderReturn, onClose }: AdminRejectReturnDialogProps) => {
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | null>(null)
  const { mutate: rejectReturn, isPending } = useAdminRejectReturn()

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!reason.trim()) {
      setError('Reason is required')
      return
    }

    if (reason.trim().length > 255) {
      setError('Reason must be at most 255 characters')
      return
    }

    setError(null)

    rejectReturn(
      { id: orderReturn.id, reason: reason.trim() },
      {
        onSuccess: () => {
          toast.success(`Return #${orderReturn.id} rejected`)
          onClose()
        },
        onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not reject the return'))
      }
    )
  }

  return (
    <Modal
      eyebrow="Reject"
      title={`Return #${orderReturn.id}`}
      description={`The customer will see this reason. ${orderReturn.customerEmail}`}
      tone="pink"
      onClose={onClose}
    >
      <form onSubmit={submit}>
        {error && (
          <div className="mb-6">
            <FormError message={error} />
          </div>
        )}
        <FormField
          label="Reason"
          value={reason}
          onChange={setReason}
          accent="pink"
          autoComplete="off"
          disabled={isPending}
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
