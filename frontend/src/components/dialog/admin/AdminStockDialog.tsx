import { useState, type SubmitEvent } from 'react'
import { toast } from 'sonner'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { useAdminAdjustStock } from '@/hooks/admin/useAdminAdjustStock'
import { useAdminSetStock } from '@/hooks/admin/useAdminSetStock'
import type { AdminStockItemResponse } from '@/types/admin/adminStock'
import { getErrorMessage } from '@/utils/error'
import { formatSize } from '@/utils/format'

interface AdminStockDialogProps {
  item: AdminStockItemResponse
  onClose: () => void
}

type Mode = 'ADJUST' | 'SET'

const MODES: { value: Mode; label: string }[] = [
  { value: 'ADJUST', label: 'Adjust by' },
  { value: 'SET', label: 'Set to' }
]

export const AdminStockDialog = ({ item, onClose }: AdminStockDialogProps) => {
  const [mode, setMode] = useState<Mode>('ADJUST')
  const [value, setValue] = useState('')
  const [error, setError] = useState<string | null>(null)
  const { mutate: adjustStock, isPending: isAdjusting } = useAdminAdjustStock()
  const { mutate: setStock, isPending: isSetting } = useAdminSetStock()

  const amount = Number(value)
  const isValidAmount = value.trim() !== '' && Number.isInteger(amount)
  const newStock = mode === 'ADJUST' ? item.stock + amount : amount
  const isPending = isAdjusting || isSetting

  const selectMode = (next: Mode) => {
    setMode(next)
    setValue('')
    setError(null)
  }

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!isValidAmount) {
      setError('Enter a whole number')
      return
    }

    if (Math.abs(amount) > 100000) {
      setError('Value must be between -100000 and 100000')
      return
    }

    if (newStock < 0) {
      setError('Stock cannot go below zero')
      return
    }

    setError(null)

    const callbacks = {
      onSuccess: () => {
        toast.success('Stock updated')
        onClose()
      },
      onError: (mutationError: unknown) => setError(getErrorMessage(mutationError, 'Could not update the stock'))
    }

    if (mode === 'ADJUST') {
      adjustStock({ skuId: item.skuId, delta: amount }, callbacks)
    } else {
      setStock({ skuId: item.skuId, stock: amount }, callbacks)
    }
  }

  return (
    <Modal
      eyebrow="Stock"
      title={item.productName}
      description={`${formatSize(item.size)} · ${item.color}`}
      onClose={onClose}
    >
      <form onSubmit={submit}>
        <p className="font-mono text-xs uppercase tracking-widest">
          Current stock: <span className="text-neon-cyan">{item.stock}</span>
        </p>
        <div className="mt-4 flex gap-2">
          {MODES.map((option) => (
            <button
              key={option.value}
              type="button"
              aria-pressed={mode === option.value}
              onClick={() => selectMode(option.value)}
              className={`border px-4 py-2 font-mono text-[10px] uppercase tracking-widest transition-colors ${
                mode === option.value
                  ? 'border-neon-cyan bg-neon-cyan/10 text-neon-cyan'
                  : 'border-border text-muted-foreground hover:border-foreground/40'
              }`}
            >
              {option.label}
            </button>
          ))}
        </div>
        <input
          type="number"
          step={1}
          value={value}
          onChange={(event) => setValue(event.target.value)}
          aria-label={mode === 'ADJUST' ? 'Amount to adjust the stock by' : 'New stock value'}
          placeholder={mode === 'ADJUST' ? 'e.g. 10 or -2' : 'e.g. 25'}
          className="mt-4 w-full border border-border bg-background px-3 py-2.5 font-mono text-sm outline-none focus:border-neon-cyan"
        />
        <p className="mt-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          New stock: {isValidAmount && newStock >= 0 ? newStock : '—'}
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
