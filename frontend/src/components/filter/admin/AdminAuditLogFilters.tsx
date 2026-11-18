import { AdminFilterSelect, type FilterOption } from '@/components/filter/admin/AdminFilterSelect'
import { AdminFilterBar } from '@/components/filter/admin/AdminFilterBar'
import { AUDIT_ACTION_LABELS, AUDIT_ENTITY_TYPE_LABELS } from '@/constants'

const ENTITY_TYPE_OPTIONS: FilterOption[] = [
  { value: '', label: 'All types' },
  ...Object.entries(AUDIT_ENTITY_TYPE_LABELS).map(([value, label]) => ({ value, label }))
]

const ACTION_OPTIONS: FilterOption[] = [
  { value: '', label: 'All actions' },
  ...Object.entries(AUDIT_ACTION_LABELS).map(([value, label]) => ({ value, label }))
]

export const AdminAuditLogFilters = () => (
  <AdminFilterBar searchPlaceholder="Search by user or item…">
    <AdminFilterSelect param="entityType" label="Entity type" options={ENTITY_TYPE_OPTIONS} />
    <AdminFilterSelect param="action" label="Action" options={ACTION_OPTIONS} />
  </AdminFilterBar>
)
