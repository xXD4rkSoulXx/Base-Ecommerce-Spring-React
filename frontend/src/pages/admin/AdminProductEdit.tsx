import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { toast } from 'sonner'
import { StatusBadge } from '@/components/badge/StatusBadge'
import { AdminProductForm } from '@/components/form/admin/AdminProductForm'
import { AdminProductVariantsSection } from '@/components/section/admin/AdminProductVariantsSection'
import { AdminBackLink } from '@/components/ui/admin/AdminBackLink'
import { FormError } from '@/components/ui/FormError'
import { useAdminArchiveProduct } from '@/hooks/admin/useAdminArchiveProduct'
import { useAdminProduct } from '@/hooks/admin/useAdminProduct'
import { useAdminRestoreProduct } from '@/hooks/admin/useAdminRestoreProduct'
import { useAdminUpdateProduct } from '@/hooks/admin/useAdminUpdateProduct'
import { usePermission } from '@/hooks/usePermission'
import type { AdminProductRequest } from '@/types/admin/adminProduct'
import { getErrorMessage } from '@/utils/error'

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
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not update the product'))
    })
  }

  const archive = () => {
    setError(null)

    archiveProduct(undefined, {
      onSuccess: () => toast.success('Product archived'),
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not archive the product'))
    })
  }

  const restore = () => {
    setError(null)

    restoreProduct(undefined, {
      onSuccess: () => toast.success('Product restored'),
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not restore the product'))
    })
  }

  return (
    <div className="p-6 md:p-10">
      <AdminBackLink to="/admin/products">Products</AdminBackLink>
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">
            {product ? product.name : 'Product'}
          </h1>
          {product && !product.active && (
            <div className="mt-3">
              <StatusBadge tone="muted">Archived · hidden from the shop</StatusBadge>
            </div>
          )}
        </div>
        {product && canEdit && (
          <button
            type="button"
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
          <AdminProductVariantsSection
            productId={product.id}
            productRef={product.ref}
            productCategory={product.category}
          />
        </>
      )}
    </div>
  )
}
