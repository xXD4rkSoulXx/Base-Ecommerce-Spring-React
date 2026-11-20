import { zodResolver } from '@hookform/resolvers/zod'
import { useId } from 'react'
import { useForm } from 'react-hook-form'
import { Field } from '@/components/ui/Field'
import { FieldError } from '@/components/ui/FieldError'
import { FormError } from '@/components/ui/FormError'
import { SelectField } from '@/components/ui/SelectField'
import { PRODUCT_CATEGORIES } from '@/constants'
import { productSchema, type ProductFormValues } from '@/schemas/admin/adminProduct'
import type { AdminProductDetailsResponse, AdminProductRequest } from '@/types/admin/adminProduct'
import { formatCategory } from '@/utils/format'

interface AdminProductFormProps {
  product?: AdminProductDetailsResponse
  submitLabel: string
  isPending: boolean
  error: string | null
  disabled?: boolean
  onSubmit: (request: AdminProductRequest) => void
}

const SLOT_OPTIONS: { value: ProductFormValues['featuredSlot']; label: string }[] = [
  { value: '', label: 'None' },
  { value: 'LEFT', label: 'Left' },
  { value: 'RIGHT', label: 'Right' }
]

export const AdminProductForm = ({
  product,
  submitLabel,
  isPending,
  error,
  disabled = false,
  onSubmit
}: AdminProductFormProps) => {
  const descriptionErrorId = useId()

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<ProductFormValues>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      ref: product?.ref ?? '',
      name: product?.name ?? '',
      description: product?.description ?? '',
      price: product ? product.price.toString() : '',
      discountPercentage: product?.discountPercentage ?? 0,
      category: product?.category,
      featuredSlot: product?.featuredSlot ?? ''
    }
  })

  const submit = (data: ProductFormValues) =>
    onSubmit({
      ...data,
      price: Number(data.price.replace(',', '.')),
      featuredSlot: data.featuredSlot || undefined
    })

  return (
    <form onSubmit={handleSubmit(submit)} noValidate className="max-w-2xl space-y-5">
      <div className="grid gap-5 sm:grid-cols-2">
        <Field label="Ref" accent="cyan" disabled={disabled} error={errors.ref?.message} {...register('ref')} />
        <Field label="Name" accent="cyan" disabled={disabled} error={errors.name?.message} {...register('name')} />
      </div>
      <div>
        <label className="block">
          <span className="block font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
            Description
          </span>
          <textarea
            rows={6}
            disabled={disabled}
            aria-invalid={!!errors.description}
            aria-describedby={errors.description ? descriptionErrorId : undefined}
            className={`mt-2 w-full border bg-card px-4 py-3 text-sm transition-colors focus:border-neon-cyan focus:outline-none disabled:opacity-50 ${
              errors.description ? 'border-destructive' : 'border-border'
            }`}
            {...register('description')}
          />
        </label>
        <FieldError id={descriptionErrorId} message={errors.description?.message} />
      </div>
      <div className="grid gap-5 sm:grid-cols-2">
        <Field
          label="Price (€)"
          inputMode="decimal"
          accent="cyan"
          disabled={disabled}
          error={errors.price?.message}
          {...register('price')}
        />
        <Field
          label="Discount (%)"
          type="number"
          accent="cyan"
          disabled={disabled}
          error={errors.discountPercentage?.message}
          {...register('discountPercentage', { valueAsNumber: true })}
        />
      </div>
      <div className="grid gap-5 sm:grid-cols-2">
        <SelectField label="Category" disabled={disabled} error={errors.category?.message} {...register('category')}>
          <option value="">Select a category</option>
          {PRODUCT_CATEGORIES.map((option) => (
            <option key={option} value={option}>
              {formatCategory(option)}
            </option>
          ))}
        </SelectField>
        <SelectField
          label="Featured on the home page"
          disabled={disabled}
          error={errors.featuredSlot?.message}
          {...register('featuredSlot')}
        >
          {SLOT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </SelectField>
      </div>
      {error && <FormError message={error} />}
      {!disabled && (
        <button
          type="submit"
          disabled={isPending}
          className="bg-neon-cyan px-8 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
        >
          {submitLabel}
        </button>
      )}
    </form>
  )
}
