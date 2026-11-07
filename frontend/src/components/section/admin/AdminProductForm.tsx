import { useState, type SubmitEvent } from 'react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
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

const CATEGORY_OPTIONS: ProductCategory[] = [
  'OUTERWEAR',
  'TOPS',
  'BOTTOMS',
  'FOOTWEAR',
  'UNDERWEAR',
  'ACCESSORIES'
]

const SLOT_OPTIONS: { value: FeaturedSlot | ''; label: string }[] = [
  { value: '', label: 'None' },
  { value: 'LEFT', label: 'Left' },
  { value: 'RIGHT', label: 'Right' }
]

const formatCategory = (category: string) => category.charAt(0) + category.slice(1).toLowerCase()

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

  const submit = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()

    const parsedPrice = Number(price.replace(',', '.'))
    const parsedDiscount = Number(discount)

    if (!ref.trim() || !name.trim() || !description.trim()) {
      setValidationError('Fill in the ref, name and description')
      return
    }

    if (!category) {
      setValidationError('Select a category')
      return
    }

    if (!price.trim() || !Number.isFinite(parsedPrice) || parsedPrice <= 0) {
      setValidationError('Enter a valid price')
      return
    }

    if (!Number.isInteger(parsedDiscount) || parsedDiscount < 0 || parsedDiscount > 100) {
      setValidationError('Discount must be a whole number between 0 and 100')
      return
    }

    setValidationError(null)

    onSubmit({
      ref,
      name,
      description,
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
          onChange={(e) => setDescription(e.target.value)}
          rows={6}
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
            onChange={(e) => setCategory(e.target.value as ProductCategory | '')}
            disabled={disabled}
            className="w-full border border-border bg-background px-4 py-3 text-sm focus:border-neon-cyan focus:outline-none disabled:opacity-50"
          >
            <option value="">Select a category</option>
            {CATEGORY_OPTIONS.map((option) => (
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
            onChange={(e) => setFeaturedSlot(e.target.value as FeaturedSlot | '')}
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
