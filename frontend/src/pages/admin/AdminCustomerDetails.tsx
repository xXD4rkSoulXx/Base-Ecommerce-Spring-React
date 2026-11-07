import { useEffect } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminCustomer } from '@/hooks/admin/useAdminCustomer'
import { useAdminCustomerOrders } from '@/hooks/admin/useAdminCustomerOrders'
import { usePermission } from '@/hooks/usePermission'

const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium' })
const formatPrice = (amount: number, currency: string) =>
  new Intl.NumberFormat('pt-PT', { style: 'currency', currency }).format(amount)

export const AdminCustomerDetails = () => {
  const { id } = useParams()
  const customerId = Number(id)
  const [searchParams, setSearchParams] = useSearchParams()
  const { data: customer, isLoading, isError } = useAdminCustomer(customerId)
  const { data: ordersPage, isError: isOrdersError } = useAdminCustomerOrders(customerId)
  const canViewOrders = usePermission('ORDERS_READ')

  const urlPage = Number(searchParams.get('page')) || 1
  const currentPage = urlPage < 1 ? 1 : urlPage
  const orders = ordersPage?.content ?? []
  const totalPages = ordersPage?.totalPages ?? 0

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
      <Link
        to="/admin/customers"
        className="mb-6 inline-flex items-center gap-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Customers
      </Link>
      {isError && <FormError message="Could not load the customer" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {customer && (
        <>
          <h1 className="mb-8 font-display text-5xl uppercase tracking-tighter md:text-7xl">
            {customer.firstName} {customer.lastName}
          </h1>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div className="border border-border bg-card p-4">
              <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Email</p>
              <p className="mt-2 break-all text-sm">{customer.email}</p>
            </div>
            <div className="border border-border bg-card p-4">
              <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Joined</p>
              <p className="mt-2 text-sm">{dateFormatter.format(new Date(customer.createdAt))}</p>
            </div>
            <div className="border border-border bg-card p-4">
              <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Orders</p>
              <p className="mt-2 font-mono text-sm">{customer.orders}</p>
            </div>
            <div className="border border-border bg-card p-4">
              <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Last order</p>
              <p className="mt-2 text-sm">
                {customer.lastOrderAt ? dateFormatter.format(new Date(customer.lastOrderAt)) : '—'}
              </p>
            </div>
          </div>
          <h2 className="mb-6 mt-12 font-display text-3xl uppercase tracking-tighter md:text-4xl">Orders</h2>
          {isOrdersError && <FormError message="Could not load the orders" />}
          {ordersPage && (
            <div className="overflow-x-auto border border-border bg-card">
              <table className="w-full min-w-[600px] text-sm">
                <thead>
                  <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                    <th className="p-4">Order</th>
                    <th className="p-4">Date</th>
                    <th className="p-4">Status</th>
                    <th className="p-4 text-right">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.length === 0 ? (
                    <tr>
                      <td
                        colSpan={4}
                        className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground"
                      >
                        No orders yet
                      </td>
                    </tr>
                  ) : (
                    orders.map((order) => (
                      <tr key={order.id} className="border-b border-border last:border-b-0">
                        <td className="p-4 font-mono">
                          {canViewOrders ? (
                            <Link to={`/admin/orders/${order.id}`} className="hover:text-neon-cyan">
                              #{order.id}
                            </Link>
                          ) : (
                            <span>#{order.id}</span>
                          )}
                        </td>
                        <td className="p-4 font-mono text-xs text-muted-foreground">
                          {dateFormatter.format(new Date(order.createdAt))}
                        </td>
                        <td className="p-4">
                          <span className="inline-block border border-border px-2 py-0.5 font-mono text-[10px] uppercase tracking-widest">
                            {order.status.replaceAll('_', ' ')}
                          </span>
                        </td>
                        <td className="p-4 text-right font-mono text-neon-cyan">
                          {formatPrice(order.totalAmount, order.currency)}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
          <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
        </>
      )}
    </div>
  )
}
