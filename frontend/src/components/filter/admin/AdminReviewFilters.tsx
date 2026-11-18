import { AdminFilterSelect, type FilterOption } from '@/components/filter/admin/AdminFilterSelect'
import { AdminFilterBar } from '@/components/filter/admin/AdminFilterBar'

const VISIBILITY_OPTIONS: FilterOption[] = [
  { value: '', label: 'All reviews' },
  { value: 'false', label: 'Visible' },
  { value: 'true', label: 'Hidden' }
]

export const AdminReviewFilters = () => (
  <AdminFilterBar searchPlaceholder="Search by product, customer or title…">
    <AdminFilterSelect param="hidden" label="Visibility" options={VISIBILITY_OPTIONS} />
  </AdminFilterBar>
)
