import { useState } from 'react'
import { toast } from 'sonner'
import { AdminVariantDialog } from '@/components/dialog/admin/AdminVariantDialog'
import { AdminVariantImagesDialog } from '@/components/dialog/admin/AdminVariantImagesDialog'
import { AdminVariantRow } from '@/components/row/admin/AdminVariantRow'
import { FormError } from '@/components/ui/FormError'
import { useAdminProductSkus } from '@/hooks/admin/useAdminProductSkus'
import { useAdminArchiveProductSku } from '@/hooks/admin/useAdminArchiveProductSku'
import { useAdminRestoreProductSku } from '@/hooks/admin/useAdminRestoreProductSku'
import { usePermission } from '@/hooks/usePermission'
import { getErrorMessage } from '@/utils/error'
import type { AdminProductSkuResponse } from '@/types/admin/adminProductSku'
import type { ProductCategory } from '@/types/product'

interface AdminProductVariantsSectionProps {
  productId: number
  productRef: string
  productCategory: ProductCategory
}

export const AdminProductVariantsSection = ({
  productId,
  productRef,
  productCategory
}: AdminProductVariantsSectionProps) => {
  const { data: variants, isLoading, isError } = useAdminProductSkus(productId)
  const { mutate: archiveSku, isPending: isArchiving } = useAdminArchiveProductSku(productId)
  const { mutate: restoreSku, isPending: isRestoring } = useAdminRestoreProductSku(productId)
  const canEdit = usePermission('PRODUCTS_WRITE')
  const canViewStock = usePermission('STOCK_READ')
  const [editing, setEditing] = useState<{ variant: AdminProductSkuResponse | null } | null>(null)
  const [managingImages, setManagingImages] = useState<AdminProductSkuResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  const archive = (variant: AdminProductSkuResponse) => {
    setError(null)

    archiveSku(variant.id, {
      onSuccess: () => toast.success('Variant archived'),
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not archive the variant'))
    })
  }

  const restore = (variant: AdminProductSkuResponse) => {
    setError(null)

    restoreSku(variant.id, {
      onSuccess: () => toast.success('Variant restored'),
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not restore the variant'))
    })
  }

  return (
    <section className="mt-16">
      <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
        <h2 className="font-display text-3xl uppercase tracking-tighter md:text-4xl">
          {variants ? `Variants (${variants.length})` : 'Variants'}
        </h2>
        {canEdit && (
          <button
            type="button"
            onClick={() => setEditing({ variant: null })}
            className="bg-neon-cyan px-6 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
          >
            Add variant
          </button>
        )}
      </div>
      {isError && (
        <div className="mb-4">
          <FormError message="Could not load the variants" />
        </div>
      )}
      {error && (
        <div className="mb-4">
          <FormError message={error} />
        </div>
      )}
      {isLoading && <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>}
      {variants && (
        <div className="overflow-x-auto border border-border bg-card">
          <table className="w-full min-w-[800px] text-sm">
            <thead>
              <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                <th className="p-4">Variant</th>
                <th className="p-4 text-right">Stock</th>
                <th className="p-4">Weight</th>
                <th className="p-4">Dimensions</th>
                <th className="p-4">Discount</th>
                <th className="p-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {variants.length === 0 ? (
                <tr>
                  <td
                    colSpan={6}
                    className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground"
                  >
                    No variants yet
                  </td>
                </tr>
              ) : (
                variants.map((variant) => (
                  <AdminVariantRow
                    key={variant.id}
                    variant={variant}
                    productRef={productRef}
                    canEdit={canEdit}
                    canViewStock={canViewStock}
                    isBusy={isArchiving || isRestoring}
                    onImages={setManagingImages}
                    onEdit={(selected) => setEditing({ variant: selected })}
                    onArchive={archive}
                    onRestore={restore}
                  />
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
      {editing && (
        <AdminVariantDialog
          key={editing.variant?.id ?? 'new'}
          productId={productId}
          productCategory={productCategory}
          variant={editing.variant}
          onClose={() => setEditing(null)}
        />
      )}
      {managingImages && (
        <AdminVariantImagesDialog
          key={managingImages.id}
          productId={productId}
          variant={managingImages}
          onClose={() => setManagingImages(null)}
        />
      )}
    </section>
  )
}
