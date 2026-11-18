import { useState } from 'react'
import { toast } from 'sonner'
import { AdminDiscountCodeDialog } from '@/components/dialog/admin/AdminDiscountCodeDialog'
import { AdminDiscountCodeFilters } from '@/components/filter/admin/AdminDiscountCodeFilters'
import { AdminDiscountCodeRow } from '@/components/row/admin/AdminDiscountCodeRow'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { AdminTable, type AdminTableColumn } from '@/components/ui/admin/AdminTable'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminDiscountCodes } from '@/hooks/admin/useAdminDiscountCodes'
import { useAdminSetDiscountCodeActive } from '@/hooks/admin/useAdminSetDiscountCodeActive'
import { usePagination } from '@/hooks/usePagination'
import { usePermission } from '@/hooks/usePermission'
import type { AdminDiscountCodeResponse } from '@/types/admin/adminDiscountCode'
import { getErrorMessage } from '@/utils/error'

const COLUMNS: AdminTableColumn[] = [
  { label: 'Code' },
  { label: 'Discount' },
  { label: 'Orders' },
  { label: 'Status' },
  { label: 'Created' },
  { label: 'Action', alignRight: true }
]

export const AdminDiscounts = () => {
  const { data, isLoading, isError } = useAdminDiscountCodes()
  const { mutate: setActive, isPending } = useAdminSetDiscountCodeActive()
  const canEdit = usePermission('DISCOUNTS_WRITE')
  const [editing, setEditing] = useState<{ discountCode: AdminDiscountCodeResponse | null } | null>(null)
  const [error, setError] = useState<string | null>(null)

  const discountCodes = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const { currentPage, changePage } = usePagination(totalPages)

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
      <AdminPageTitle
        count={data?.totalElements}
        action={
          canEdit && (
            <button
              type="button"
              onClick={() => setEditing({ discountCode: null })}
              className="bg-neon-cyan px-6 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
            >
              New code
            </button>
          )
        }
      >
        Discounts
      </AdminPageTitle>
      <AdminDiscountCodeFilters />
      {isError && <FormError message="Could not load the discount codes" />}
      {error && <FormError message={error} />}
      {isLoading && <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>}
      {data && (
        <AdminTable
          columns={COLUMNS}
          minWidthClassName="min-w-[700px]"
          empty={discountCodes.length === 0}
          emptyMessage="No discount codes found"
        >
          {discountCodes.map((discountCode) => (
            <AdminDiscountCodeRow
              key={discountCode.id}
              discountCode={discountCode}
              canEdit={canEdit}
              isBusy={isPending}
              onEdit={(selected) => setEditing({ discountCode: selected })}
              onToggle={toggle}
            />
          ))}
        </AdminTable>
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
