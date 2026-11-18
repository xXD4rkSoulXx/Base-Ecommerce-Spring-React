import { useState } from 'react'
import { toast } from 'sonner'
import { AdminReviewFilters } from '@/components/filter/admin/AdminReviewFilters'
import { AdminReviewRow } from '@/components/row/admin/AdminReviewRow'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { AdminTable, type AdminTableColumn } from '@/components/ui/admin/AdminTable'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminReviews } from '@/hooks/admin/useAdminReviews'
import { useAdminSetReviewHidden } from '@/hooks/admin/useAdminSetReviewHidden'
import { usePagination } from '@/hooks/usePagination'
import { usePermission } from '@/hooks/usePermission'
import type { AdminReviewResponse } from '@/types/admin/adminReview'
import { getErrorMessage } from '@/utils/error'

const COLUMNS: AdminTableColumn[] = [
  { label: 'Product' },
  { label: 'Customer' },
  { label: 'Review' },
  { label: 'Status' },
  { label: 'Date' },
  { label: 'Action', alignRight: true }
]

export const AdminReviews = () => {
  const { data, isLoading, isError } = useAdminReviews()
  const { mutate: setHidden, isPending } = useAdminSetReviewHidden()
  const canEdit = usePermission('REVIEWS_WRITE')
  const canViewProduct = usePermission('PRODUCTS_READ')
  const [error, setError] = useState<string | null>(null)

  const reviews = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const { currentPage, changePage } = usePagination(totalPages)

  const toggle = (review: AdminReviewResponse) => {
    setError(null)

    setHidden(
      { id: review.id, hidden: !review.hidden },
      {
        onSuccess: () => toast.success(review.hidden ? 'Review is visible again' : 'Review hidden'),
        onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not update the review'))
      }
    )
  }

  return (
    <div className="p-6 md:p-10">
      <AdminPageTitle count={data?.totalElements}>Reviews</AdminPageTitle>
      <p className="mb-6 max-w-2xl text-sm text-muted-foreground">
        Hidden reviews disappear from the shop and no longer count towards the product rating.
      </p>
      <AdminReviewFilters />
      {isError && <FormError message="Could not load the reviews" />}
      {error && <FormError message={error} />}
      {isLoading && <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>}
      {data && (
        <AdminTable
          columns={COLUMNS}
          minWidthClassName="min-w-[900px]"
          empty={reviews.length === 0}
          emptyMessage="No reviews found"
        >
          {reviews.map((review) => (
            <AdminReviewRow
              key={review.id}
              review={review}
              canEdit={canEdit}
              canViewProduct={canViewProduct}
              isBusy={isPending}
              onToggle={toggle}
            />
          ))}
        </AdminTable>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
    </div>
  )
}
