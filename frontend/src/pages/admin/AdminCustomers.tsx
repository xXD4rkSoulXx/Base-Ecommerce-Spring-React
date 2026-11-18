import { AdminFilterBar } from '@/components/filter/admin/AdminFilterBar'
import { AdminCustomerRow } from '@/components/row/admin/AdminCustomerRow'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { AdminTable, type AdminTableColumn } from '@/components/ui/admin/AdminTable'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminCustomers } from '@/hooks/admin/useAdminCustomers'
import { usePagination } from '@/hooks/usePagination'

const COLUMNS: AdminTableColumn[] = [
  { label: 'Customer' },
  { label: 'Joined' },
  { label: 'Orders' },
  { label: 'Last order' },
  { label: 'Action', alignRight: true }
]

export const AdminCustomers = () => {
  const { data, isLoading, isError } = useAdminCustomers()
  const customers = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const { currentPage, changePage } = usePagination(totalPages)

  return (
    <div className="p-6 md:p-10">
      <AdminPageTitle count={data?.totalElements}>Customers</AdminPageTitle>
      <AdminFilterBar searchPlaceholder="Search by name or email…" />
      {isError && <FormError message="Could not load the customers" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <AdminTable
          columns={COLUMNS}
          minWidthClassName="min-w-[700px]"
          empty={customers.length === 0}
          emptyMessage="No customers found"
        >
          {customers.map((customer) => (
            <AdminCustomerRow key={customer.id} customer={customer} />
          ))}
        </AdminTable>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
    </div>
  )
}
