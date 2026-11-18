import { useState, type SubmitEvent } from 'react'
import { toast } from 'sonner'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { Modal } from '@/components/ui/Modal'
import { useAdminCreateProductSku } from '@/hooks/admin/useAdminCreateProductSku'
import { useAdminUpdateProductSku } from '@/hooks/admin/useAdminUpdateProductSku'
import { getErrorMessage } from '@/utils/error'
import { formatSize } from '@/utils/format'
import type { AdminProductSkuResponse } from '@/types/admin/adminProductSku'
import type { ProductCategory, ProductColor, ProductSize } from '@/types/product'

interface AdminVariantDialogProps {
  productId: number
  productCategory: ProductCategory
  variant: AdminProductSkuResponse | null
  onClose: () => void
}

const LETTER_SIZES: ProductSize[] = ['XS', 'S', 'M', 'L', 'XL', 'XXL']
const NUMERIC_SIZES = Array.from({ length: 32 }, (_, index) => `_${index + 15}`) as ProductSize[]

const SIZES_BY_CATEGORY: Record<ProductCategory, ProductSize[]> = {
  OUTERWEAR: LETTER_SIZES,
  TOPS: LETTER_SIZES,
  BOTTOMS: LETTER_SIZES,
  UNDERWEAR: LETTER_SIZES,
  FOOTWEAR: NUMERIC_SIZES,
  ACCESSORIES: ['ONE_SIZE']
}

const COLORS: ProductColor[] = [
  'BLACK', 'BLUE', 'CYAN', 'GOLD', 'GRAY', 'GREEN', 'LIME', 'MAGENTA', 'MAROON',
  'OLIVE', 'ORANGE', 'PINK', 'PURPLE', 'RED', 'SALMON', 'TEAL', 'WHITE', 'YELLOW'
]

const isWholeNumber = (value: string, min: number, max: number) =>
  value.trim() !== '' && Number.isInteger(Number(value)) && Number(value) >= min && Number(value) <= max

export const AdminVariantDialog = ({ productId, productCategory, variant, onClose }: AdminVariantDialogProps) => {
  const sizes = SIZES_BY_CATEGORY[productCategory]
  const [size, setSize] = useState<ProductSize | ''>(variant?.size ?? (sizes.length === 1 ? sizes[0] : ''))
  const [color, setColor] = useState<ProductColor | ''>(variant?.color ?? '')
  const [weight, setWeight] = useState(variant ? variant.weightGrams.toString() : '')
  const [length, setLength] = useState(variant ? variant.lengthCm.toString() : '')
  const [width, setWidth] = useState(variant ? variant.widthCm.toString() : '')
  const [height, setHeight] = useState(variant ? variant.heightCm.toString() : '')
  const [discount, setDiscount] = useState(variant ? variant.discountPercentage.toString() : '0')
  const [error, setError] = useState<string | null>(null)
  const { mutate: createSku, isPending: isCreating } = useAdminCreateProductSku(productId)
  const { mutate: updateSku, isPending: isUpdating } = useAdminUpdateProductSku(productId)
  const isPending = isCreating || isUpdating

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!isWholeNumber(weight, 1, 100000)) {
      setError('Weight must be a whole number between 1 and 100000 g')
      return
    }

    if (![length, width, height].every((value) => isWholeNumber(value, 1, 1000))) {
      setError('Dimensions must be whole numbers between 1 and 1000 cm')
      return
    }

    if (!isWholeNumber(discount, 0, 100)) {
      setError('Discount must be a whole number between 0 and 100')
      return
    }

    setError(null)

    const details = {
      weightGrams: Number(weight),
      lengthCm: Number(length),
      widthCm: Number(width),
      heightCm: Number(height),
      discountPercentage: Number(discount)
    }

    const callbacks = {
      onSuccess: () => {
        toast.success(variant ? 'Variant updated' : 'Variant added')
        onClose()
      },
      onError: (mutationError: unknown) => setError(getErrorMessage(mutationError, 'Could not save the variant'))
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
    <Modal eyebrow="Variant" title={variant ? 'Edit variant' : 'Add variant'} size="lg" onClose={onClose}>
      <form onSubmit={submit}>
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="mb-2 block font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              Size
            </span>
            <select
              value={size}
              onChange={(event) => setSize(event.target.value as ProductSize | '')}
              disabled={variant !== null}
              className="w-full border border-border bg-background px-4 py-3 text-sm focus:border-neon-cyan focus:outline-none disabled:opacity-50"
            >
              <option value="">Select a size</option>
              {sizes.map((option) => (
                <option key={option} value={option}>
                  {formatSize(option)}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              Color
            </span>
            <select
              value={color}
              onChange={(event) => setColor(event.target.value as ProductColor | '')}
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
          The discount override replaces the product discount when it is greater than 0. Stock starts at 0 and is
          managed on the Stock page.
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
