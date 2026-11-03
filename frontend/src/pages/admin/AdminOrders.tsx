import { useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { AdminOrderFilters } from '@/components/section/admin/AdminOrderFilters'
import { AdminOrderRow } from '@/components/ui/admin/AdminOrderRow'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminOrders } from '@/hooks/admin/useAdminOrders'

export const AdminOrders = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const { data, isLoading, isError } = useAdminOrders()
  const isTodoEmpty =
    (searchParams.get('status') || ['admin', 'order']) === 'TODO' && !searchParams.get('q')

  const urlPage = Number(searchParams.get('page')) || 1
  const currentPage = urlPage < 1 ? 1 : urlPage
  const orders = data?.content ?? []
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

  return (
    <div className="p-6 md:p-10">
      <h1 className="mb-8 font-display text-5xl uppercase tracking-tighter md:text-7xl">
        {data ? `Orders (${data.totalElements})` : 'Orders'}
      </h1>
      <AdminOrderFilters />
      {isError && <FormError message="Could not load the orders" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <div className="overflow-x-auto border border-border bg-card">
          <table className="w-full min-w-[800px] text-sm">
            <thead>
              <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                <th className="p-4">Order</th>
                <th className="p-4">Customer</th>
                <th className="p-4">Date</th>
                <th className="p-4">Status</th>
                <th className="p-4 text-right">Total</th>
                <th className="p-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 ? (
                <tr>
                  <td
                    colSpan={6}
                    className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground"
                  >
                    {isTodoEmpty ? 'Nothing waiting for action' : 'No orders found'}
                  </td>
                </tr>
              ) : (
                orders.map((order) => <AdminOrderRow key={order.id} order={order} />)
              )}
            </tbody>
          </table>
        </div>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
    </div>
  )
}
