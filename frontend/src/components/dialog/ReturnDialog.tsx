import { useState } from 'react'
import { isAxiosError } from 'axios'
import { Package, RotateCcw, X } from 'lucide-react'
import { toast } from 'sonner'
import { ReturnItemRow } from '@/components/ui/ReturnItemRow'
import { useRequestReturn } from '@/hooks/useRequestReturn'
import type { OrderDetailsResponse, ReturnReason } from '@/types/order'

const returnReasonLabels: Record<ReturnReason, string> = {
  DOES_NOT_FIT: 'Does not fit',
  DEFECTIVE: 'Defective or damaged',
  NOT_AS_DESCRIBED: 'Not as described',
  CHANGED_MIND: 'Changed my mind',
  OTHER: 'Other'
}
const returnReasons = Object.entries(returnReasonLabels) as [ReturnReason, string][]

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? error.response?.data?.message ?? fallback : fallback

interface ReturnDialogProps {
  order: OrderDetailsResponse
  onClose: () => void
}

export const ReturnDialog = ({ order, onClose }: ReturnDialogProps) => {
  const { mutate, isPending, error } = useRequestReturn(order.id)
  const [quantities, setQuantities] = useState<Record<number, number>>({})
  const [reason, setReason] = useState<ReturnReason | ''>('')
  const [comment, setComment] = useState('')

  const returnableItems = order.items.filter((item) => item.returnableQuantity > 0)
  const selectedItems = returnableItems.filter((item) => (quantities[item.id] ?? 0) > 0)
  const missingComment = reason === 'OTHER' && comment.trim() === ''
  const canSubmit = selectedItems.length > 0 && reason !== '' && !missingComment && !isPending

  const handleSubmit = () => {
    if (reason === '') {
      return
    }

    mutate(
      {
        reason,
        comment: comment.trim() || undefined,
        items: selectedItems.map((item) => ({ orderItemId: item.id, quantity: quantities[item.id] }))
      },
      {
        onSuccess: () => {
          toast.success('Return requested')
          onClose()
        }
      }
    )
  }

  return (
    <div className="fixed inset-0 z-[80] flex items-center justify-center p-4" aria-modal>
      <div onClick={onClose} className="absolute inset-0 bg-background/85 backdrop-blur-sm" />
      <div className="relative z-10 flex max-h-[90vh] w-full max-w-lg flex-col border border-neon-cyan/30 bg-card shadow-2xl shadow-neon-cyan/10">
        <div className="flex items-start justify-between border-b border-border px-5 py-4">
          <div>
            <h3 className="font-display text-2xl uppercase tracking-tighter">Return items</h3>
            <p className="mt-1 font-mono text-xs text-muted-foreground">Order #{order.id}</p>
          </div>
          <button
            onClick={onClose}
            className="text-muted-foreground hover:text-foreground"
            aria-label="Close"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
        <div className="flex-1 space-y-6 overflow-y-auto p-5">
          <div>
            <div className="mb-3 flex items-center gap-2">
              <Package className="h-3.5 w-3.5 text-neon-cyan" />
              <h4 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                Select items
              </h4>
            </div>
            <div className="border border-border">
              {returnableItems.map((item) => (
                <ReturnItemRow
                  key={item.id}
                  item={item}
                  quantity={quantities[item.id] ?? 0}
                  onChange={(quantity) => setQuantities((previous) => ({ ...previous, [item.id]: quantity }))}
                />
              ))}
            </div>
          </div>
          <div className="space-y-3">
            <h4 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
              Reason
            </h4>
            <select
              value={reason}
              onChange={(event) => setReason(event.target.value as ReturnReason | '')}
              className="w-full border border-border bg-background px-3 py-2 font-mono text-xs"
            >
              <option value="">Select a reason</option>
              {returnReasons.map(([value, label]) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </select>
            <textarea
              value={comment}
              onChange={(event) => setComment(event.target.value)}
              maxLength={500}
              rows={3}
              placeholder={reason === 'OTHER' ? 'Please describe the reason (required)' : 'Comment (optional)'}
              className="w-full resize-none border border-border bg-background px-3 py-2 font-mono text-xs"
            />
          </div>
          <p className="font-mono text-[10px] leading-relaxed text-muted-foreground">
            Return shipping is free. You will be refunded to your original payment method, and the
            shipping cost is refunded in proportion to the weight of the items you return.
          </p>
          {error && (
            <p className="font-mono text-xs uppercase tracking-widest text-neon-pink">
              {getErrorMessage(error, 'Failed to request return')}
            </p>
          )}
        </div>
        <div className="border-t border-border p-4">
          <button
            type="button"
            onClick={handleSubmit}
            disabled={!canSubmit}
            className="flex w-full items-center justify-center gap-2 border border-neon-cyan bg-neon-cyan/10 py-2.5 font-mono text-xs uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan/20 disabled:opacity-40"
          >
            <RotateCcw className="h-3.5 w-3.5" />
            {isPending ? 'Requesting...' : 'Request return'}
          </button>
        </div>
      </div>
    </div>
  )
}
