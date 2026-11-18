import { useState, type SubmitEvent } from 'react'
import { Package, RotateCcw } from 'lucide-react'
import { toast } from 'sonner'
import { ReturnItemRow } from '@/components/row/ReturnItemRow'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { SectionTitle } from '@/components/ui/SectionTitle'
import { RETURN_REASON_LABELS } from '@/constants'
import { useRequestReturn } from '@/hooks/useRequestReturn'
import { getErrorMessage } from '@/utils/error'
import type { OrderDetailsResponse, ReturnReason } from '@/types/order'

interface ReturnDialogProps {
  order: OrderDetailsResponse
  onClose: () => void
}

const RETURN_REASONS = Object.entries(RETURN_REASON_LABELS) as [ReturnReason, string][]

export const ReturnDialog = ({ order, onClose }: ReturnDialogProps) => {
  const { mutate, isPending, error } = useRequestReturn(order.id)
  const [quantities, setQuantities] = useState<Record<number, number>>({})
  const [reason, setReason] = useState<ReturnReason | ''>('')
  const [comment, setComment] = useState('')

  const returnableItems = order.items.filter((item) => item.returnableQuantity > 0)
  const selectedItems = returnableItems.filter((item) => (quantities[item.id] ?? 0) > 0)
  const missingComment = reason === 'OTHER' && comment.trim() === ''
  const canSubmit = selectedItems.length > 0 && reason !== '' && !missingComment && !isPending

  const handleSubmit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!canSubmit) {
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
    <Modal eyebrow={`Order #${order.id}`} title="Return items" size="lg" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <SectionTitle as="h3" icon={<Package className="h-3.5 w-3.5" />}>
            Select items
          </SectionTitle>
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
          <h3 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Reason</h3>
          <select
            value={reason}
            onChange={(event) => setReason(event.target.value as ReturnReason | '')}
            aria-label="Reason"
            className="w-full border border-border bg-background px-3 py-2 font-mono text-xs"
          >
            <option value="">Select a reason</option>
            {RETURN_REASONS.map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
          <textarea
            value={comment}
            onChange={(event) => setComment(event.target.value)}
            aria-label="Comment"
            maxLength={500}
            rows={3}
            placeholder={reason === 'OTHER' ? 'Please describe the reason (required)' : 'Comment (optional)'}
            className="w-full resize-none border border-border bg-background px-3 py-2 font-mono text-xs"
          />
        </div>
        <p className="font-mono text-[10px] leading-relaxed text-muted-foreground">
          Return shipping is free. You will be refunded to your original payment method, and the shipping cost is
          refunded in proportion to the weight of the items you return.
        </p>
        {error && <FormError message={getErrorMessage(error, 'Failed to request return')} />}
        <button
          type="submit"
          disabled={!canSubmit}
          className="flex w-full items-center justify-center gap-2 border border-neon-cyan bg-neon-cyan/10 py-2.5 font-mono text-xs uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan/20 disabled:opacity-40"
        >
          <RotateCcw className="h-3.5 w-3.5" />
          {isPending ? 'Requesting...' : 'Request return'}
        </button>
      </form>
    </Modal>
  )
}
