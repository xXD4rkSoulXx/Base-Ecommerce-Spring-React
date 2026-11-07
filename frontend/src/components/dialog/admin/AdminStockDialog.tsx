import { useState, type SubmitEvent } from 'react'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { X } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { useAdminAdjustStock } from '@/hooks/admin/useAdminAdjustStock'
import { useAdminSetStock } from '@/hooks/admin/useAdminSetStock'
import type { AdminStockItemResponse } from '@/types/admin/adminStock'

interface AdminStockDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  item: AdminStockItemResponse | null
}

type Mode = 'ADJUST' | 'SET'

const MODES: { value: Mode; label: string }[] = [
  { value: 'ADJUST', label: 'Adjust by' },
  { value: 'SET', label: 'Set to' }
]

const formatSize = (size: string) => (size.startsWith('_') ? size.substring(1) : size)

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminStockDialog = ({ open, onOpenChange, item }: AdminStockDialogProps) => {
  const [mode, setMode] = useState<Mode>('ADJUST')
  const [value, setValue] = useState('')
  const [error, setError] = useState<string | null>(null)
  const { mutate: adjustStock, isPending: isAdjusting } = useAdminAdjustStock()
  const { mutate: setStock, isPending: isSetting } = useAdminSetStock()

  if (!open || !item) {
    return null
  }

  const amount = Number(value)
  const isValidAmount = value.trim() !== '' && Number.isInteger(amount)
  const newStock = mode === 'ADJUST' ? item.stock + amount : amount
  const isPending = isAdjusting || isSetting

  const close = () => {
    setMode('ADJUST')
    setValue('')
    setError(null)
    onOpenChange(false)
  }

  const selectMode = (next: Mode) => {
    setMode(next)
    setValue('')
    setError(null)
  }

  const submit = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (!isValidAmount) {
      setError('Enter a whole number')
      return
    }

    if (Math.abs(amount) > 100000) {
      setError(`Value must be between -100000} and 100000`)
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
        close()
      },
      onError: (mutationError: unknown) =>
        setError(getErrorMessage(mutationError, 'Could not update the stock'))
    }

    if (mode === 'ADJUST') {
      adjustStock({ skuId: item.skuId, delta: amount }, callbacks)
    } else {
      setStock({ skuId: item.skuId, stock: amount }, callbacks)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <form
        onSubmit={submit}
        className="w-full max-w-md border border-neon-cyan/40 bg-card p-6 shadow-2xl shadow-neon-cyan/10"
      >
        <div className="flex items-start justify-between">
          <div>
            <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-neon-cyan">Stock</p>
            <h2 className="mt-2 font-display text-2xl uppercase tracking-tighter">{item.productName}</h2>
            <p className="mt-1 font-mono text-xs text-muted-foreground">
              {formatSize(item.size)} · {item.color}
            </p>
          </div>
          <button type="button" onClick={close} className="text-muted-foreground hover:text-foreground">
            <X className="h-5 w-5" />
          </button>
        </div>
        <p className="mt-4 font-mono text-xs uppercase tracking-widest">
          Current stock: <span className="text-neon-cyan">{item.stock}</span>
        </p>
        <div className="mt-4 flex gap-2">
          {MODES.map((option) => (
            <button
              key={option.value}
              type="button"
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
          onChange={(e) => setValue(e.target.value)}
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
            onClick={close}
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
