import { useState, type SubmitEvent } from 'react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { PRODUCT_CATEGORIES } from '@/constants'
import { formatCategory } from '@/utils/format'
import type { AdminProductDetailsResponse, AdminProductRequest } from '@/types/admin/adminProduct'
import type { FeaturedSlot, ProductCategory } from '@/types/product'

interface AdminProductFormProps {
  product?: AdminProductDetailsResponse
  submitLabel: string
  isPending: boolean
  error: string | null
  disabled?: boolean
  onSubmit: (request: AdminProductRequest) => void
}

const SLOT_OPTIONS: { value: FeaturedSlot | ''; label: string }[] = [
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
  const [ref, setRef] = useState(product?.ref ?? '')
  const [name, setName] = useState(product?.name ?? '')
  const [description, setDescription] = useState(product?.description ?? '')
  const [price, setPrice] = useState(product ? product.price.toString() : '')
  const [discount, setDiscount] = useState(product ? product.discountPercentage.toString() : '0')
  const [category, setCategory] = useState<ProductCategory | ''>(product?.category ?? '')
  const [featuredSlot, setFeaturedSlot] = useState<FeaturedSlot | ''>(product?.featuredSlot ?? '')
  const [validationError, setValidationError] = useState<string | null>(null)

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    const trimmedRef = ref.trim()
    const trimmedName = name.trim()
    const trimmedDescription = description.trim()
    const trimmedPrice = price.trim()
    const parsedPrice = Number(trimmedPrice.replace(',', '.'))
    const parsedDiscount = Number(discount)

    if (!trimmedRef || !trimmedName || !trimmedDescription) {
      setValidationError('Fill in the ref, name and description')
      return
    }

    if (trimmedRef.length > 50 || !/^[A-Za-z0-9-]+$/.test(trimmedRef)) {
      setValidationError('Ref can only contain letters, numbers and dashes, up to 50 characters')
      return
    }

    if (trimmedName.length > 120) {
      setValidationError('Name must be at most 120 characters')
      return
    }

    if (trimmedDescription.length > 5000) {
      setValidationError('Description must be at most 5000 characters')
      return
    }

    if (!category) {
      setValidationError('Select a category')
      return
    }

    if (!/^\d{1,5}([.,]\d{1,2})?$/.test(trimmedPrice) || parsedPrice < 0.01) {
      setValidationError('Price must be between 0.01 and 99999.99, with at most 2 decimal places')
      return
    }

    if (discount.trim() === '' || !Number.isInteger(parsedDiscount) || parsedDiscount < 0 || parsedDiscount > 100) {
      setValidationError('Discount must be a whole number between 0 and 100')
      return
    }

    setValidationError(null)

    onSubmit({
      ref: trimmedRef,
      name: trimmedName,
      description: trimmedDescription,
      price: parsedPrice,
      discountPercentage: parsedDiscount,
      category,
      featuredSlot: featuredSlot || undefined
    })
  }

  const shownError = validationError ?? error

  return (
    <form onSubmit={submit} className="max-w-2xl space-y-5">
      <div className="grid gap-5 sm:grid-cols-2">
        <FormField label="Ref" value={ref} onChange={setRef} accent="cyan" disabled={disabled} />
        <FormField label="Name" value={name} onChange={setName} accent="cyan" disabled={disabled} />
      </div>
      <label className="block">
        <span className="mb-2 block font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          Description
        </span>
        <textarea
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          rows={6}
          maxLength={5000}
          disabled={disabled}
          className="w-full border border-border bg-background px-4 py-3 text-sm focus:border-neon-cyan focus:outline-none disabled:opacity-50"
        />
      </label>
      <div className="grid gap-5 sm:grid-cols-2">
        <FormField label="Price (€)" value={price} onChange={setPrice} accent="cyan" disabled={disabled} />
        <FormField label="Discount (%)" value={discount} onChange={setDiscount} accent="cyan" disabled={disabled} />
      </div>
      <div className="grid gap-5 sm:grid-cols-2">
        <label className="block">
          <span className="mb-2 block font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            Category
          </span>
          <select
            value={category}
            onChange={(event) => setCategory(event.target.value as ProductCategory | '')}
            disabled={disabled}
            className="w-full border border-border bg-background px-4 py-3 text-sm focus:border-neon-cyan focus:outline-none disabled:opacity-50"
          >
            <option value="">Select a category</option>
            {PRODUCT_CATEGORIES.map((option) => (
              <option key={option} value={option}>
                {formatCategory(option)}
              </option>
            ))}
          </select>
        </label>
        <label className="block">
          <span className="mb-2 block font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            Featured on the home page
          </span>
          <select
            value={featuredSlot}
            onChange={(event) => setFeaturedSlot(event.target.value as FeaturedSlot | '')}
            disabled={disabled}
            className="w-full border border-border bg-background px-4 py-3 text-sm focus:border-neon-cyan focus:outline-none disabled:opacity-50"
          >
            {SLOT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
      </div>
      {shownError && <FormError message={shownError} />}
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
