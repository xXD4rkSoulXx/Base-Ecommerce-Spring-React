import { useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { AdminCustomerFilters } from '@/components/section/admin/AdminCustomerFilters'
import { AdminCustomerRow } from '@/components/ui/admin/AdminCustomerRow'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminCustomers } from '@/hooks/admin/useAdminCustomers'

export const AdminCustomers = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const { data, isLoading, isError } = useAdminCustomers()

  const urlPage = Number(searchParams.get('page')) || 1
  const currentPage = urlPage < 1 ? 1 : urlPage
  const customers = data?.content ?? []
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
        {data ? `Customers (${data.totalElements})` : 'Customers'}
      </h1>
      <AdminCustomerFilters />
      {isError && <FormError message="Could not load the customers" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <div className="overflow-x-auto border border-border bg-card">
          <table className="w-full min-w-[700px] text-sm">
            <thead>
              <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                <th className="p-4">Customer</th>
                <th className="p-4">Joined</th>
                <th className="p-4">Orders</th>
                <th className="p-4">Last order</th>
                <th className="p-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {customers.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground"
                  >
                    No customers found
                  </td>
                </tr>
              ) : (
                customers.map((customer) => <AdminCustomerRow key={customer.id} customer={customer} />)
              )}
            </tbody>
          </table>
        </div>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
    </div>
  )
}
