import { toast } from 'sonner'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { useCancelOrder } from '@/hooks/useCancelOrder'
import type { OrderDetailsResponse } from '@/types/order'
import { getErrorMessage } from '@/utils/error'
import { formatPrice } from '@/utils/format'

interface CancelOrderDialogProps {
  order: OrderDetailsResponse
  onClose: () => void
}

export const CancelOrderDialog = ({ order, onClose }: CancelOrderDialogProps) => {
  const { mutate, isPending, error } = useCancelOrder(order.id)

  const cancel = () =>
    mutate(undefined, {
      onSuccess: () => {
        toast.success('Order canceled')
        onClose()
      }
    })

  return (
    <Modal
      eyebrow="Cancel"
      title={`Order #${order.id}`}
      description="This cannot be undone."
      tone="pink"
      onClose={onClose}
    >
      <p className="text-sm leading-relaxed text-muted-foreground">
        You will be refunded {formatPrice(order.totalAmount)} to your original payment method. The items go back into
        stock and any discount code you used is released.
      </p>
      {error && (
        <div className="mt-6">
          <FormError message={getErrorMessage(error, 'Could not cancel the order')} />
        </div>
      )}
      <div className="mt-8 flex gap-3">
        <button
          type="button"
          onClick={onClose}
          disabled={isPending}
          className="flex-1 border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-foreground disabled:opacity-50"
        >
          Keep order
        </button>
        <button
          type="button"
          onClick={cancel}
          disabled={isPending}
          className="flex-1 bg-neon-pink py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
        >
          {isPending ? 'Canceling...' : 'Cancel order'}
        </button>
      </div>
    </Modal>
  )
}
