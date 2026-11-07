import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { AdminDiscountCodeDialog } from '@/components/dialog/admin/AdminDiscountCodeDialog'
import { AdminDiscountCodeFilters } from '@/components/section/admin/AdminDiscountCodeFilters'
import { AdminDiscountCodeRow } from '@/components/ui/admin/AdminDiscountCodeRow'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminDiscountCodes } from '@/hooks/admin/useAdminDiscountCodes'
import { useAdminSetDiscountCodeActive } from '@/hooks/admin/useAdminSetDiscountCodeActive'
import { usePermission } from '@/hooks/usePermission'
import type { AdminDiscountCodeResponse } from '@/types/admin/adminDiscountCode'

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminDiscounts = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const { data, isLoading, isError } = useAdminDiscountCodes()
  const { mutate: setActive, isPending } = useAdminSetDiscountCodeActive()
  const canEdit = usePermission('DISCOUNTS_WRITE')
  const [editing, setEditing] = useState<{ discountCode: AdminDiscountCodeResponse | null } | null>(null)
  const [error, setError] = useState<string | null>(null)

  const urlPage = Number(searchParams.get('page')) || 1
  const currentPage = urlPage < 1 ? 1 : urlPage
  const discountCodes = data?.content ?? []
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

  const toggle = (discountCode: AdminDiscountCodeResponse) => {
    setError(null)

    setActive(
      { id: discountCode.id, active: !discountCode.active },
      {
        onSuccess: () => toast.success(discountCode.active ? 'Code deactivated' : 'Code activated'),
        onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not update the code'))
      }
    )
  }

  return (
    <div className="p-6 md:p-10">
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">
          {data ? `Discounts (${data.totalElements})` : 'Discounts'}
        </h1>
        {canEdit && (
          <button
            onClick={() => setEditing({ discountCode: null })}
            className="bg-neon-cyan px-6 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
          >
            New code
          </button>
        )}
      </div>
      <AdminDiscountCodeFilters />
      {isError && <FormError message="Could not load the discount codes" />}
      {error && <FormError message={error} />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <div className="overflow-x-auto border border-border bg-card">
          <table className="w-full min-w-[700px] text-sm">
            <thead>
              <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                <th className="p-4">Code</th>
                <th className="p-4">Discount</th>
                <th className="p-4">Orders</th>
                <th className="p-4">Status</th>
                <th className="p-4">Created</th>
                <th className="p-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {discountCodes.length === 0 ? (
                <tr>
                  <td
                    colSpan={6}
                    className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground"
                  >
                    No discount codes found
                  </td>
                </tr>
              ) : (
                discountCodes.map((discountCode) => (
                  <AdminDiscountCodeRow
                    key={discountCode.id}
                    discountCode={discountCode}
                    canEdit={canEdit}
                    isBusy={isPending}
                    onEdit={(selected) => setEditing({ discountCode: selected })}
                    onToggle={toggle}
                  />
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
      {editing && (
        <AdminDiscountCodeDialog
          key={editing.discountCode?.id ?? 'new'}
          discountCode={editing.discountCode}
          onClose={() => setEditing(null)}
        />
      )}
    </div>
  )
}
