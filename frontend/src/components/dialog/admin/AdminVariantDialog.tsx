import { useState, type SubmitEvent } from 'react'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { X } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { useAdminCreateProductSku } from '@/hooks/admin/useAdminCreateProductSku'
import { useAdminUpdateProductSku } from '@/hooks/admin/useAdminUpdateProductSku'
import type { AdminProductSkuResponse } from '@/types/admin/adminProductSku'
import type { ProductColor, ProductSize } from '@/types/product'

interface AdminVariantDialogProps {
  productId: number
  variant: AdminProductSkuResponse | null
  onClose: () => void
}

const LETTER_SIZES: ProductSize[] = ['XS', 'S', 'M', 'L', 'XL', 'XXL']
const NUMERIC_SIZES = Array.from({ length: 32 }, (_, index) => `_${index + 15}`) as ProductSize[]
const COLORS: ProductColor[] = [
  'BLACK', 'BLUE', 'CYAN', 'GOLD', 'GRAY', 'GREEN', 'LIME', 'MAGENTA', 'MAROON',
  'OLIVE', 'ORANGE', 'PINK', 'PURPLE', 'RED', 'SALMON', 'TEAL', 'WHITE', 'YELLOW'
]

const formatSize = (size: string) => (size.startsWith('_') ? size.substring(1) : size)
const isWholeNumber = (value: string, min: number, max: number) =>
  value.trim() !== '' && Number.isInteger(Number(value)) && Number(value) >= min && Number(value) <= max

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminVariantDialog = ({ productId, variant, onClose }: AdminVariantDialogProps) => {
  const [size, setSize] = useState<ProductSize | ''>(variant?.size ?? '')
  const [color, setColor] = useState<ProductColor | ''>(variant?.color ?? '')
  const [weight, setWeight] = useState(variant ? variant.weight.toString() : '')
  const [length, setLength] = useState(variant ? variant.length.toString() : '')
  const [width, setWidth] = useState(variant ? variant.width.toString() : '')
  const [height, setHeight] = useState(variant ? variant.height.toString() : '')
  const [discount, setDiscount] = useState(variant ? variant.discountPercentage.toString() : '0')
  const [error, setError] = useState<string | null>(null)
  const { mutate: createSku, isPending: isCreating } = useAdminCreateProductSku(productId)
  const { mutate: updateSku, isPending: isUpdating } = useAdminUpdateProductSku(productId)
  const isPending = isCreating || isUpdating

  const submit = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (![weight, length, width, height].every((value) => isWholeNumber(value, 1, 100000))) {
      setError('Weight and dimensions must be whole numbers greater than zero')
      return
    }

    if (!isWholeNumber(discount, 0, 100)) {
      setError('Discount must be a whole number between 0 and 100')
      return
    }

    setError(null)

    const details = {
      weight: Number(weight),
      length: Number(length),
      width: Number(width),
      height: Number(height),
      discountPercentage: Number(discount)
    }

    const callbacks = {
      onSuccess: () => {
        toast.success(variant ? 'Variant updated' : 'Variant added')
        onClose()
      },
      onError: (mutationError: unknown) =>
        setError(getErrorMessage(mutationError, 'Could not save the variant'))
    }

    if (variant) {
      updateSku({ skuId: variant.id, request: details }, callbacks)
      return
    }

    if (!size || !color) {
      setError('Select a size and a color')
      return
    }

    createSku({ size, color, ...details }, callbacks)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <form
        onSubmit={submit}
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto border border-neon-cyan/40 bg-card p-6 shadow-2xl shadow-neon-cyan/10"
      >
        <div className="flex items-start justify-between">
          <div>
            <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-neon-cyan">Variant</p>
            <h2 className="mt-2 font-display text-2xl uppercase tracking-tighter">
              {variant ? 'Edit variant' : 'Add variant'}
            </h2>
          </div>
          <button type="button" onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="mt-5 grid gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="mb-2 block font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              Size
            </span>
            <select
              value={size}
              onChange={(e) => setSize(e.target.value as ProductSize | '')}
              disabled={variant !== null}
              className="w-full border border-border bg-background px-4 py-3 text-sm focus:border-neon-cyan focus:outline-none disabled:opacity-50"
            >
              <option value="">Select a size</option>
              <optgroup label="Letter sizes">
                {LETTER_SIZES.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </optgroup>
              <optgroup label="Numeric sizes">
                {NUMERIC_SIZES.map((option) => (
                  <option key={option} value={option}>
                    {formatSize(option)}
                  </option>
                ))}
              </optgroup>
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              Color
            </span>
            <select
              value={color}
              onChange={(e) => setColor(e.target.value as ProductColor | '')}
              disabled={variant !== null}
              className="w-full border border-border bg-background px-4 py-3 text-sm focus:border-neon-cyan focus:outline-none disabled:opacity-50"
            >
              <option value="">Select a color</option>
              {COLORS.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </label>
          <FormField label="Weight (g)" value={weight} onChange={setWeight} accent="cyan" />
          <FormField label="Discount override (%)" value={discount} onChange={setDiscount} accent="cyan" />
          <FormField label="Length (cm)" value={length} onChange={setLength} accent="cyan" />
          <FormField label="Width (cm)" value={width} onChange={setWidth} accent="cyan" />
          <FormField label="Height (cm)" value={height} onChange={setHeight} accent="cyan" />
        </div>
        <p className="mt-3 text-xs text-muted-foreground">
          The discount override replaces the product discount when it is greater than 0. Stock starts at 0
          and is managed on the Stock page.
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
