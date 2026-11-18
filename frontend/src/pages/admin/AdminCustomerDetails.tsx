import { useParams } from 'react-router-dom'
import { AdminInfoCard } from '@/components/card/admin/AdminInfoCard'
import { AdminCustomerOrderRow } from '@/components/row/admin/AdminCustomerOrderRow'
import { AdminBackLink } from '@/components/ui/admin/AdminBackLink'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { AdminTable, type AdminTableColumn } from '@/components/ui/admin/AdminTable'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminCustomer } from '@/hooks/admin/useAdminCustomer'
import { useAdminCustomerOrders } from '@/hooks/admin/useAdminCustomerOrders'
import { usePagination } from '@/hooks/usePagination'
import { formatDate } from '@/utils/format'

const ORDER_COLUMNS: AdminTableColumn[] = [
  { label: 'Order' },
  { label: 'Date' },
  { label: 'Status' },
  { label: 'Total', alignRight: true }
]

export const AdminCustomerDetails = () => {
  const { id } = useParams()
  const customerId = Number(id)
  const { data: customer, isLoading, isError } = useAdminCustomer(customerId)
  const { data: ordersPage, isError: isOrdersError } = useAdminCustomerOrders(customerId)
  const orders = ordersPage?.content ?? []
  const totalPages = ordersPage?.totalPages ?? 0
  const { currentPage, changePage } = usePagination(totalPages)

  return (
    <div className="p-6 md:p-10">
      <AdminBackLink to="/admin/customers">Customers</AdminBackLink>
      {isError && <FormError message="Could not load the customer" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {customer && (
        <>
          <AdminPageTitle>
            {customer.firstName} {customer.lastName}
          </AdminPageTitle>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <AdminInfoCard label="Email" valueClassName="break-all">
              {customer.email}
            </AdminInfoCard>
            <AdminInfoCard label="Joined">{formatDate(customer.createdAt)}</AdminInfoCard>
            <AdminInfoCard label="Orders" valueClassName="font-mono">
              {customer.orders}
            </AdminInfoCard>
            <AdminInfoCard label="Last order">
              {customer.lastOrderAt ? formatDate(customer.lastOrderAt) : '—'}
            </AdminInfoCard>
          </div>
          <h2 className="mb-6 mt-12 font-display text-3xl uppercase tracking-tighter md:text-4xl">Orders</h2>
          {isOrdersError && <FormError message="Could not load the orders" />}
          {ordersPage && (
            <AdminTable
              columns={ORDER_COLUMNS}
              minWidthClassName="min-w-[600px]"
              empty={orders.length === 0}
              emptyMessage="No orders yet"
            >
              {orders.map((order) => (
                <AdminCustomerOrderRow key={order.id} order={order} />
              ))}
            </AdminTable>
          )}
          <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
        </>
      )}
    </div>
  )
}
