import { useState } from 'react'
import { AdminStockDialog } from '@/components/dialog/admin/AdminStockDialog'
import { AdminStockFilters } from '@/components/filter/admin/AdminStockFilters'
import { AdminStockRow } from '@/components/row/admin/AdminStockRow'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { AdminTable, type AdminTableColumn } from '@/components/ui/admin/AdminTable'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminStock } from '@/hooks/admin/useAdminStock'
import { usePagination } from '@/hooks/usePagination'
import { usePermission } from '@/hooks/usePermission'
import type { AdminStockItemResponse } from '@/types/admin/adminStock'

const COLUMNS: AdminTableColumn[] = [
  { label: 'Product' },
  { label: 'Variant' },
  { label: 'Stock', alignRight: true },
  { label: 'Action', alignRight: true }
]

export const AdminStock = () => {
  const { data, isLoading, isError } = useAdminStock()
  const canEdit = usePermission('STOCK_WRITE')
  const [editing, setEditing] = useState<AdminStockItemResponse | null>(null)

  const items = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const { currentPage, changePage } = usePagination(totalPages)

  return (
    <div className="p-6 md:p-10">
      <AdminPageTitle count={data?.totalElements}>Stock</AdminPageTitle>
      <AdminStockFilters />
      {isError && <FormError message="Could not load the stock" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <AdminTable
          columns={COLUMNS}
          minWidthClassName="min-w-[700px]"
          empty={items.length === 0}
          emptyMessage="No variants found"
        >
          {items.map((item) => (
            <AdminStockRow key={item.skuId} item={item} canEdit={canEdit} onEdit={setEditing} />
          ))}
        </AdminTable>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
      {editing && <AdminStockDialog item={editing} onClose={() => setEditing(null)} />}
    </div>
  )
}
