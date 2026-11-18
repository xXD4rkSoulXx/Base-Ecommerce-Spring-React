import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { toast } from 'sonner'
import { FieldError } from '@/components/ui/FieldError'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { useAdminAdjustStock } from '@/hooks/admin/useAdminAdjustStock'
import { useAdminSetStock } from '@/hooks/admin/useAdminSetStock'
import { stockSchema, type StockFormValues } from '@/schemas/admin/adminStock'
import type { AdminStockItemResponse } from '@/types/admin/adminStock'
import { getErrorMessage } from '@/utils/error'
import { formatSize } from '@/utils/format'

interface AdminStockDialogProps {
  item: AdminStockItemResponse
  onClose: () => void
}

const MODES: { value: StockFormValues['mode']; label: string }[] = [
  { value: 'ADJUST', label: 'Adjust by' },
  { value: 'SET', label: 'Set to' }
]

export const AdminStockDialog = ({ item, onClose }: AdminStockDialogProps) => {
  const {
    register,
    control,
    handleSubmit,
    setValue,
    resetField,
    formState: { errors }
  } = useForm<StockFormValues>({
    resolver: zodResolver(stockSchema(item.stock)),
    defaultValues: { mode: 'ADJUST' }
  })

  const { mutate: adjustStock, isPending: isAdjusting, error: adjustError } = useAdminAdjustStock()
  const { mutate: setStock, isPending: isSetting, error: setStockError } = useAdminSetStock()
  const isPending = isAdjusting || isSetting
  const error = adjustError ?? setStockError

  const mode = useWatch({ control, name: 'mode' })
  const amount = useWatch({ control, name: 'amount' })
  const isValidAmount = Number.isInteger(amount)
  const newStock = mode === 'ADJUST' ? item.stock + amount : amount

  const selectMode = (next: StockFormValues['mode']) => {
    setValue('mode', next)
    resetField('amount')
  }

  const onSubmit = (data: StockFormValues) => {
    const callbacks = {
      onSuccess: () => {
        toast.success('Stock updated')
        onClose()
      }
    }

    if (data.mode === 'ADJUST') {
      adjustStock({ skuId: item.skuId, delta: data.amount }, callbacks)
    } else {
      setStock({ skuId: item.skuId, stock: data.amount }, callbacks)
    }
  }

  return (
    <Modal
      eyebrow="Stock"
      title={item.productName}
      description={`${formatSize(item.size)} · ${item.color}`}
      onClose={onClose}
    >
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
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
          aria-label={mode === 'ADJUST' ? 'Amount to adjust the stock by' : 'New stock value'}
          aria-invalid={!!errors.amount}
          placeholder={mode === 'ADJUST' ? 'e.g. 10 or -2' : 'e.g. 25'}
          className="mt-4 w-full border border-border bg-background px-3 py-2.5 font-mono text-sm outline-none focus:border-neon-cyan"
          {...register('amount', { valueAsNumber: true })}
        />
        <FieldError message={errors.amount?.message} />
        <p className="mt-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          New stock: {isValidAmount && newStock >= 0 ? newStock : '—'}
        </p>
        {error && (
          <div className="mt-4">
            <FormError message={getErrorMessage(error, 'Could not update the stock')} />
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
