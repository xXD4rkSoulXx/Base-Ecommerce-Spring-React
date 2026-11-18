import { AdminAuditLogFilters } from '@/components/filter/admin/AdminAuditLogFilters'
import { AdminAuditLogRow } from '@/components/row/admin/AdminAuditLogRow'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { AdminTable, type AdminTableColumn } from '@/components/ui/admin/AdminTable'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminAuditLogs } from '@/hooks/admin/useAdminAuditLogs'
import { usePagination } from '@/hooks/usePagination'

const COLUMNS: AdminTableColumn[] = [
  { label: 'When' },
  { label: 'Who' },
  { label: 'Action' },
  { label: 'Item' },
  { label: 'Details' }
]

export const AdminAuditLog = () => {
  const { data, isLoading, isError } = useAdminAuditLogs()
  const auditLogs = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const { currentPage, changePage } = usePagination(totalPages)

  return (
    <div className="p-6 md:p-10">
      <AdminPageTitle count={data?.totalElements}>Audit log</AdminPageTitle>
      <p className="mb-6 max-w-2xl text-sm text-muted-foreground">
        Every change made in the admin area, newest first. Records cannot be edited or deleted.
      </p>
      <AdminAuditLogFilters />
      {isError && <FormError message="Could not load the audit log" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <AdminTable
          columns={COLUMNS}
          minWidthClassName="min-w-[900px]"
          empty={auditLogs.length === 0}
          emptyMessage="No records found"
        >
          {auditLogs.map((auditLog) => (
            <AdminAuditLogRow key={auditLog.id} auditLog={auditLog} />
          ))}
        </AdminTable>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
    </div>
  )
}
