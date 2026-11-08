import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { AdminReviewFilters } from '@/components/section/admin/AdminReviewFilters'
import { AdminReviewRow } from '@/components/ui/admin/AdminReviewRow'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminReviews } from '@/hooks/admin/useAdminReviews'
import { useAdminSetReviewHidden } from '@/hooks/admin/useAdminSetReviewHidden'
import { usePermission } from '@/hooks/usePermission'
import type { AdminReviewResponse } from '@/types/admin/adminReview'

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminReviews = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const { data, isLoading, isError } = useAdminReviews()
  const { mutate: setHidden, isPending } = useAdminSetReviewHidden()
  const canEdit = usePermission('REVIEWS_WRITE')
  const canViewProduct = usePermission('PRODUCTS_READ')
  const [error, setError] = useState<string | null>(null)

  const urlPage = Number(searchParams.get('page')) || 1
  const currentPage = urlPage < 1 ? 1 : urlPage
  const reviews = data?.content ?? []
  const totalPages = data?.totalPages ?? 0

  useEffect(() => {
    if (totalPages > 0 && urlPage > totalPages) {
      const next = new URLSearchParams(searchParams)

      next.set('page', totalPages.toString())
      setSearchParams(next, { replace: true })
    }
  }, [urlPage, totalPages, searchParams, setSearchParams])

  const changePage = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      const next = new URLSearchParams(searchParams)

      next.set('page', page.toString())
      setSearchParams(next)
    }
  }

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
      <h1 className="mb-8 font-display text-5xl uppercase tracking-tighter md:text-7xl">
        {data ? `Reviews (${data.totalElements})` : 'Reviews'}
      </h1>
      <p className="mb-6 max-w-2xl text-sm text-muted-foreground">
        Hidden reviews disappear from the shop and no longer count towards the product rating.
      </p>
      <AdminReviewFilters />
      {isError && <FormError message="Could not load the reviews" />}
      {error && <FormError message={error} />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <div className="overflow-x-auto border border-border bg-card">
          <table className="w-full min-w-[900px] text-sm">
            <thead>
              <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                <th className="p-4">Product</th>
                <th className="p-4">Customer</th>
                <th className="p-4">Review</th>
                <th className="p-4">Status</th>
                <th className="p-4">Date</th>
                <th className="p-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {reviews.length === 0 ? (
                <tr>
                  <td
                    colSpan={6}
                    className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground"
                  >
                    No reviews found
                  </td>
                </tr>
              ) : (
                reviews.map((review) => (
                  <AdminReviewRow
                    key={review.id}
                    review={review}
                    canEdit={canEdit}
                    canViewProduct={canViewProduct}
                    isBusy={isPending}
                    onToggle={toggle}
                  />
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
    </div>
  )
}
