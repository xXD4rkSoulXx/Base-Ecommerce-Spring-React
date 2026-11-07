import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { ArrowLeft } from 'lucide-react'
import { AdminProductForm } from '@/components/section/admin/AdminProductForm'
import { AdminProductVariants } from '@/components/section/admin/AdminProductVariants'
import { FormError } from '@/components/ui/FormError'
import { useAdminProduct } from '@/hooks/admin/useAdminProduct'
import { useAdminArchiveProduct } from '@/hooks/admin/useAdminArchiveProduct'
import { useAdminRestoreProduct } from '@/hooks/admin/useAdminRestoreProduct'
import { useAdminUpdateProduct } from '@/hooks/admin/useAdminUpdateProduct'
import { usePermission } from '@/hooks/usePermission'
import type { AdminProductRequest } from '@/types/admin/adminProduct'

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminProductEdit = () => {
  const { id } = useParams()
  const productId = Number(id)
  const { data: product, isLoading, isError } = useAdminProduct(productId)
  const { mutate: updateProduct, isPending } = useAdminUpdateProduct(productId)
  const { mutate: archiveProduct, isPending: isArchiving } = useAdminArchiveProduct(productId)
  const { mutate: restoreProduct, isPending: isRestoring } = useAdminRestoreProduct(productId)
  const canEdit = usePermission('PRODUCTS_WRITE')
  const [error, setError] = useState<string | null>(null)

  const submit = (request: AdminProductRequest) => {
    setError(null)

    updateProduct(request, {
      onSuccess: () => toast.success('Product updated'),
      onError: (mutationError) =>
        setError(getErrorMessage(mutationError, 'Could not update the product'))
    })
  }

  const archive = () => {
    setError(null)

    archiveProduct(undefined, {
      onSuccess: () => toast.success('Product archived'),
      onError: (mutationError) =>
        setError(getErrorMessage(mutationError, 'Could not archive the product'))
    })
  }

  const restore = () => {
    setError(null)

    restoreProduct(undefined, {
      onSuccess: () => toast.success('Product restored'),
      onError: (mutationError) =>
        setError(getErrorMessage(mutationError, 'Could not restore the product'))
    })
  }

  return (
    <div className="p-6 md:p-10">
      <Link
        to="/admin/products"
        className="mb-6 inline-flex items-center gap-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Products
      </Link>
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">
            {product ? product.name : 'Product'}
          </h1>
          {product && !product.active && (
            <span className="mt-3 inline-block border border-border px-2 py-1 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              Archived · hidden from the shop
            </span>
          )}
        </div>
        {product && canEdit && (
          <button
            onClick={product.active ? archive : restore}
            disabled={isArchiving || isRestoring}
            className={`border px-6 py-3 text-xs font-black uppercase tracking-widest hover:opacity-80 disabled:opacity-50 ${
              product.active ? 'border-neon-pink text-neon-pink' : 'border-neon-cyan text-neon-cyan'
            }`}
          >
            {product.active ? 'Archive product' : 'Restore product'}
          </button>
        )}
      </div>
      {isError && <FormError message="Could not load the product" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {product && (
        <>
          <AdminProductForm
            key={product.id}
            product={product}
            submitLabel="Save changes"
            isPending={isPending}
            error={error}
            disabled={!canEdit}
            onSubmit={submit}
          />
          <AdminProductVariants productId={product.id} productRef={product.ref} />
        </>
      )}
    </div>
  )
}
