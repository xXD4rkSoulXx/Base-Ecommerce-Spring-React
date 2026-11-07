import { useState, type SubmitEvent } from 'react'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { X } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { useAdminCreateDiscountCode } from '@/hooks/admin/useAdminCreateDiscountCode'
import { useAdminUpdateDiscountCode } from '@/hooks/admin/useAdminUpdateDiscountCode'
import type { AdminDiscountCodeResponse } from '@/types/admin/adminDiscountCode'

interface AdminDiscountCodeDialogProps {
  discountCode: AdminDiscountCodeResponse | null
  onClose: () => void
}

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminDiscountCodeDialog = ({ discountCode, onClose }: AdminDiscountCodeDialogProps) => {
  const [code, setCode] = useState('')
  const [percentage, setPercentage] = useState(discountCode ? discountCode.discountPercentage.toString() : '')
  const [error, setError] = useState<string | null>(null)
  const { mutate: createCode, isPending: isCreating } = useAdminCreateDiscountCode()
  const { mutate: updateCode, isPending: isUpdating } = useAdminUpdateDiscountCode()
  const isPending = isCreating || isUpdating

  const submit = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()

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

    if (!/^[A-Z0-9_-]{3,20}$/.test(code)) {
      setError('Code must have 3 to 20 characters: letters, numbers, - or _')
      return
    }

    createCode({ code, discountPercentage }, callbacks)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <form
        onSubmit={submit}
        className="w-full max-w-md border border-neon-cyan/40 bg-card p-6 shadow-2xl shadow-neon-cyan/10"
      >
        <div className="flex items-start justify-between">
          <div>
            <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-neon-cyan">Discount code</p>
            <h2 className="mt-2 font-display text-2xl uppercase tracking-tighter">
              {discountCode ? 'Edit code' : 'New code'}
            </h2>
          </div>
          <button type="button" onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="mt-5 space-y-4">
          {discountCode ? (
            <div>
              <p className="mb-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Code</p>
              <p className="border border-border bg-background px-4 py-3 font-mono text-sm">
                {discountCode.code}
              </p>
            </div>
          ) : (
            <FormField
              label="Code"
              value={code}
              onChange={(value) => setCode(value.toUpperCase())}
              accent="cyan"
            />
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
    </div>
  )
}
