import { useSearchParams } from 'react-router-dom'
import { AdminOrderFilters } from '@/components/filter/admin/AdminOrderFilters'
import { AdminOrderRow } from '@/components/row/admin/AdminOrderRow'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { AdminTable, type AdminTableColumn } from '@/components/ui/admin/AdminTable'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { TODO_FILTER } from '@/constants'
import { useAdminOrders } from '@/hooks/admin/useAdminOrders'
import { usePagination } from '@/hooks/usePagination'

const COLUMNS: AdminTableColumn[] = [
  { label: 'Order' },
  { label: 'Customer' },
  { label: 'Date' },
  { label: 'Status' },
  { label: 'Total', alignRight: true },
  { label: 'Action', alignRight: true }
]

export const AdminOrders = () => {
  const [searchParams] = useSearchParams()
  const { data, isLoading, isError } = useAdminOrders()
  const isTodoEmpty = (searchParams.get('status') || TODO_FILTER) === TODO_FILTER && !searchParams.get('q')

  const orders = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const { currentPage, changePage } = usePagination(totalPages)

  return (
    <div className="p-6 md:p-10">
      <AdminPageTitle count={data?.totalElements}>Orders</AdminPageTitle>
      <AdminOrderFilters />
      {isError && <FormError message="Could not load the orders" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <AdminTable
          columns={COLUMNS}
          minWidthClassName="min-w-[800px]"
          empty={orders.length === 0}
          emptyMessage={isTodoEmpty ? 'Nothing waiting for action' : 'No orders found'}
        >
          {orders.map((order) => (
            <AdminOrderRow key={order.id} order={order} />
          ))}
        </AdminTable>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
    </div>
  )
}
