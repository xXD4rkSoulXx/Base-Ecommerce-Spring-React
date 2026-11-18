import { Link } from 'react-router-dom'
import { AdminProductFilters } from '@/components/filter/admin/AdminProductFilters'
import { AdminProductRow } from '@/components/row/admin/AdminProductRow'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { AdminTable, type AdminTableColumn } from '@/components/ui/admin/AdminTable'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminProducts } from '@/hooks/admin/useAdminProducts'
import { usePagination } from '@/hooks/usePagination'
import { usePermission } from '@/hooks/usePermission'

const COLUMNS: AdminTableColumn[] = [
  { label: 'Product' },
  { label: 'Category' },
  { label: 'Price', alignRight: true },
  { label: 'Stock', alignRight: true },
  { label: 'Status' },
  { label: 'Action', alignRight: true }
]

export const AdminProducts = () => {
  const { data, isLoading, isError } = useAdminProducts()
  const canEdit = usePermission('PRODUCTS_WRITE')

  const products = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const { currentPage, changePage } = usePagination(totalPages)

  return (
    <div className="p-6 md:p-10">
      <AdminPageTitle
        count={data?.totalElements}
        action={
          canEdit && (
            <Link
              to="/admin/products/new"
              className="bg-neon-cyan px-6 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
            >
              New product
            </Link>
          )
        }
      >
        Products
      </AdminPageTitle>
      <AdminProductFilters />
      {isError && <FormError message="Could not load the products" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <AdminTable
          columns={COLUMNS}
          minWidthClassName="min-w-[800px]"
          empty={products.length === 0}
          emptyMessage="No products found"
        >
          {products.map((product) => (
            <AdminProductRow key={product.id} product={product} canEdit={canEdit} />
          ))}
        </AdminTable>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
    </div>
  )
}
