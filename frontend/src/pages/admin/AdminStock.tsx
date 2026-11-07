import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { AdminStockDialog } from '@/components/dialog/admin/AdminStockDialog'
import { AdminStockFilters } from '@/components/section/admin/AdminStockFilters'
import { AdminStockRow } from '@/components/ui/admin/AdminStockRow'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminStock } from '@/hooks/admin/useAdminStock'
import { usePermission } from '@/hooks/usePermission'
import type { AdminStockItemResponse } from '@/types/admin/adminStock'

export const AdminStock = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const { data, isLoading, isError } = useAdminStock()
  const canEdit = usePermission('STOCK_WRITE')
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<AdminStockItemResponse | null>(null)

  const urlPage = Number(searchParams.get('page')) || 1
  const currentPage = urlPage < 1 ? 1 : urlPage
  const items = data?.content ?? []
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

  const openEdit = (item: AdminStockItemResponse) => {
    setEditing(item)
    setDialogOpen(true)
  }

  return (
    <div className="p-6 md:p-10">
      <h1 className="mb-8 font-display text-5xl uppercase tracking-tighter md:text-7xl">
        {data ? `Stock (${data.totalElements})` : 'Stock'}
      </h1>
      <AdminStockFilters />
      {isError && <FormError message="Could not load the stock" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <div className="overflow-x-auto border border-border bg-card">
          <table className="w-full min-w-[700px] text-sm">
            <thead>
              <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                <th className="p-4">Product</th>
                <th className="p-4">Variant</th>
                <th className="p-4 text-right">Stock</th>
                <th className="p-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {items.length === 0 ? (
                <tr>
                  <td
                    colSpan={4}
                    className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground"
                  >
                    No variants found
                  </td>
                </tr>
              ) : (
                items.map((item) => (
                  <AdminStockRow key={item.skuId} item={item} canEdit={canEdit} onEdit={openEdit} />
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
      <AdminStockDialog
        key={editing?.skuId ?? 'none'}
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        item={editing}
      />
    </div>
  )
}
