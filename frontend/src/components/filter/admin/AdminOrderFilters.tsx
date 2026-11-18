import { AdminFilterSelect, type FilterOption } from '@/components/filter/admin/AdminFilterSelect'
import { AdminFilterBar } from '@/components/filter/admin/AdminFilterBar'
import { ALL_FILTER, ORDER_STATUS_LABELS, TODO_FILTER } from '@/constants'

const STATUS_OPTIONS: FilterOption[] = [
  { value: ALL_FILTER, label: 'All statuses' },
  { value: TODO_FILTER, label: 'To do' },
  ...Object.entries(ORDER_STATUS_LABELS).map(([value, label]) => ({ value, label }))
]

export const AdminOrderFilters = () => (
  <AdminFilterBar searchPlaceholder="Search by order #, email or name…">
    <AdminFilterSelect param="status" label="Status" options={STATUS_OPTIONS} defaultValue={TODO_FILTER} />
  </AdminFilterBar>
)
