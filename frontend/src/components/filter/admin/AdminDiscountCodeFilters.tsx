import { AdminFilterSelect, type FilterOption } from '@/components/filter/admin/AdminFilterSelect'
import { AdminFilterBar } from '@/components/filter/admin/AdminFilterBar'

const STATUS_OPTIONS: FilterOption[] = [
  { value: '', label: 'All statuses' },
  { value: 'true', label: 'Active' },
  { value: 'false', label: 'Inactive' }
]

export const AdminDiscountCodeFilters = () => (
  <AdminFilterBar searchPlaceholder="Search by code…">
    <AdminFilterSelect param="active" label="Status" options={STATUS_OPTIONS} />
  </AdminFilterBar>
)
