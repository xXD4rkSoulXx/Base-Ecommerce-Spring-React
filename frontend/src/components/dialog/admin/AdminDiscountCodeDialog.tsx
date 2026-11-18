import { useState, type SubmitEvent } from 'react'
import { toast } from 'sonner'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { Modal } from '@/components/ui/Modal'
import { useAdminCreateDiscountCode } from '@/hooks/admin/useAdminCreateDiscountCode'
import { useAdminUpdateDiscountCode } from '@/hooks/admin/useAdminUpdateDiscountCode'
import { getErrorMessage } from '@/utils/error'
import type { AdminDiscountCodeResponse } from '@/types/admin/adminDiscountCode'

interface AdminDiscountCodeDialogProps {
  discountCode: AdminDiscountCodeResponse | null
  onClose: () => void
}

export const AdminDiscountCodeDialog = ({ discountCode, onClose }: AdminDiscountCodeDialogProps) => {
  const [code, setCode] = useState('')
  const [percentage, setPercentage] = useState(discountCode ? discountCode.discountPercentage.toString() : '')
  const [error, setError] = useState<string | null>(null)
  const { mutate: createCode, isPending: isCreating } = useAdminCreateDiscountCode()
  const { mutate: updateCode, isPending: isUpdating } = useAdminUpdateDiscountCode()
  const isPending = isCreating || isUpdating

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    const normalizedCode = code.trim()

    if (!discountCode && !/^[A-Z0-9_-]{3,20}$/.test(normalizedCode)) {
      setError('Code must have 3 to 20 characters: letters, numbers, - or _')
      return
    }

    const discountPercentage = Number(percentage)

    if (
      percentage.trim() === '' ||
      !Number.isInteger(discountPercentage) ||
      discountPercentage < 1 ||
      discountPercentage > 100
    ) {
      setError('Discount must be a whole number between 1 and 100')
      return
    }

    setError(null)

    const callbacks = {
      onSuccess: () => {
        toast.success(discountCode ? 'Code updated' : 'Code created')
        onClose()
      },
      onError: (mutationError: unknown) => setError(getErrorMessage(mutationError, 'Could not save the code'))
    }

    if (discountCode) {
      updateCode({ id: discountCode.id, request: { discountPercentage } }, callbacks)
      return
    }

    createCode({ code: normalizedCode, discountPercentage }, callbacks)
  }

  return (
    <Modal eyebrow="Discount code" title={discountCode ? 'Edit code' : 'New code'} onClose={onClose}>
      <form onSubmit={submit}>
        <div className="space-y-4">
          {discountCode ? (
            <div>
              <p className="mb-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Code</p>
              <p className="border border-border bg-background px-4 py-3 font-mono text-sm">{discountCode.code}</p>
            </div>
          ) : (
            <FormField label="Code" value={code} onChange={(value) => setCode(value.toUpperCase())} accent="cyan" />
          )}
          <FormField label="Discount (%)" value={percentage} onChange={setPercentage} accent="cyan" />
        </div>
        <p className="mt-3 text-xs text-muted-foreground">
          The code cannot be changed after it is created. Changing the discount only affects future purchases.
        </p>
        {error && (
          <div className="mt-4">
            <FormError message={error} />
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
