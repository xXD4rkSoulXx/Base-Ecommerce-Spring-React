import { useState } from 'react'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { X } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { useRejectReturn } from '@/hooks/admin/useRejectReturn'
import type { AdminReturnResponse } from '@/types/admin/return'

interface RejectReturnDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  orderReturn: AdminReturnResponse | null
}

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const RejectReturnDialog = ({ open, onOpenChange, orderReturn }: RejectReturnDialogProps) => {
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | null>(null)
  const { mutate: rejectReturn, isPending } = useRejectReturn()

  if (!open || !orderReturn) {
    return null
  }

  const close = () => {
    setReason('')
    setError(null)
    onOpenChange(false)
  }

  const submit = () => {
    const trimmed = reason.trim()

    if (!trimmed) {
      setError('Reason is required')
      return
    }

    if (trimmed.length > 255) {
      setError(`Reason must be at most 255 characters`)
      return
    }

    setError(null)

    rejectReturn(
      { id: orderReturn.id, reason: trimmed },
      {
        onSuccess: () => {
          toast.success(`Return #${orderReturn.id} rejected`)
          close()
        },
        onError: (mutationError) =>
          setError(getErrorMessage(mutationError, 'Could not reject the return'))
      }
    )
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <div className="w-full max-w-md border border-neon-pink/40 bg-card p-6 shadow-2xl shadow-neon-pink/10">
        <div className="flex items-start justify-between">
          <div>
            <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-neon-pink">Reject</p>
            <h2 className="mt-2 font-display text-2xl uppercase tracking-tighter">
              Return #{orderReturn.id}
            </h2>
            <p className="mt-2 text-xs text-muted-foreground">
              The customer will see this reason. {orderReturn.customerEmail}
            </p>
          </div>
          <button
            onClick={close}
            aria-label="Close"
            className="text-muted-foreground hover:text-neon-pink"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        {error && (
          <div className="mt-6">
            <FormError message={error} />
          </div>
        )}
        <div className="mt-6">
          <FormField
            label="Reason"
            value={reason}
            onChange={setReason}
            accent="cyan"
            autoComplete="off"
            disabled={isPending}
          />
        </div>
        <div className="mt-8 flex gap-3">
          <button
            onClick={close}
            disabled={isPending}
            className="flex-1 border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-foreground disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={submit}
            disabled={isPending}
            className="flex-1 bg-neon-pink py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            Reject return
          </button>
        </div>
      </div>
    </div>
  )
}
