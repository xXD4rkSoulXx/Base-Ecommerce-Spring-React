import { AdminFilterCheckbox } from '@/components/filter/admin/AdminFilterCheckbox'
import { AdminFilterBar } from '@/components/filter/admin/AdminFilterBar'

export const AdminStockFilters = () => (
  <AdminFilterBar searchPlaceholder="Search by product name or ref…">
    <AdminFilterCheckbox param="lowStock">Low stock</AdminFilterCheckbox>
  </AdminFilterBar>
)
