import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { ReadOnlyField } from '@/components/ui/ReadOnlyField'
import { SelectField } from '@/components/ui/SelectField'
import { useAdminCreateProductSku } from '@/hooks/admin/useAdminCreateProductSku'
import { useAdminUpdateProductSku } from '@/hooks/admin/useAdminUpdateProductSku'
import { variantSchema, type VariantFormValues } from '@/schemas/admin/adminProductSku'
import type { AdminProductSkuResponse } from '@/types/admin/adminProductSku'
import type { ProductCategory, ProductColor, ProductSize } from '@/types/product'
import { getErrorMessage } from '@/utils/error'
import { formatSize } from '@/utils/format'

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
  'BLACK',
  'BLUE',
  'CYAN',
  'GOLD',
  'GRAY',
  'GREEN',
  'LIME',
  'MAGENTA',
  'MAROON',
  'OLIVE',
  'ORANGE',
  'PINK',
  'PURPLE',
  'RED',
  'SALMON',
  'TEAL',
  'WHITE',
  'YELLOW'
]

export const AdminVariantDialog = ({ productId, productCategory, variant, onClose }: AdminVariantDialogProps) => {
  const sizes = SIZES_BY_CATEGORY[productCategory]

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<VariantFormValues>({
    resolver: zodResolver(variantSchema),
    defaultValues: {
      size: variant?.size ?? (sizes.length === 1 ? sizes[0] : undefined),
      color: variant?.color,
      weightGrams: variant?.weightGrams,
      lengthCm: variant?.lengthCm,
      widthCm: variant?.widthCm,
      heightCm: variant?.heightCm,
      discountPercentage: variant?.discountPercentage ?? 0
    }
  })

  const { mutate: createSku, isPending: isCreating, error: createError } = useAdminCreateProductSku(productId)
  const { mutate: updateSku, isPending: isUpdating, error: updateError } = useAdminUpdateProductSku(productId)
  const isPending = isCreating || isUpdating
  const error = createError ?? updateError

  const onSubmit = ({ size, color, ...details }: VariantFormValues) => {
    const callbacks = {
      onSuccess: () => {
        toast.success(variant ? 'Variant updated' : 'Variant added')
        onClose()
      }
    }

    if (variant) {
      updateSku({ skuId: variant.id, request: details }, callbacks)

      return
    }

    createSku({ size, color, ...details }, callbacks)
  }

  return (
    <Modal eyebrow="Variant" title={variant ? 'Edit variant' : 'Add variant'} size="lg" onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="grid gap-4 sm:grid-cols-2">
          {variant ? (
            <>
              <ReadOnlyField label="Size" value={formatSize(variant.size)} />
              <ReadOnlyField label="Color" value={variant.color} />
            </>
          ) : (
            <>
              <SelectField label="Size" error={errors.size?.message} {...register('size')}>
                <option value="">Select a size</option>
                {sizes.map((option) => (
                  <option key={option} value={option}>
                    {formatSize(option)}
                  </option>
                ))}
              </SelectField>
              <SelectField label="Color" error={errors.color?.message} {...register('color')}>
                <option value="">Select a color</option>
                {COLORS.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </SelectField>
            </>
          )}
          <Field
            label="Weight (g)"
            type="number"
            accent="cyan"
            disabled={isPending}
            error={errors.weightGrams?.message}
            {...register('weightGrams', { valueAsNumber: true })}
          />
          <Field
            label="Discount override (%)"
            type="number"
            accent="cyan"
            disabled={isPending}
            error={errors.discountPercentage?.message}
            {...register('discountPercentage', { valueAsNumber: true })}
          />
          <Field
            label="Length (cm)"
            type="number"
            accent="cyan"
            disabled={isPending}
            error={errors.lengthCm?.message}
            {...register('lengthCm', { valueAsNumber: true })}
          />
          <Field
            label="Width (cm)"
            type="number"
            accent="cyan"
            disabled={isPending}
            error={errors.widthCm?.message}
            {...register('widthCm', { valueAsNumber: true })}
          />
          <Field
            label="Height (cm)"
            type="number"
            accent="cyan"
            disabled={isPending}
            error={errors.heightCm?.message}
            {...register('heightCm', { valueAsNumber: true })}
          />
        </div>
        <p className="mt-3 text-xs text-muted-foreground">
          The discount override replaces the product discount when it is greater than 0. Stock starts at 0 and is
          managed on the Stock page.
        </p>
        {error && (
          <div className="mt-4">
            <FormError message={getErrorMessage(error, 'Could not save the variant')} />
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
