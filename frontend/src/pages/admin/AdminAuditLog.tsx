import { useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { AdminAuditLogFilters } from '@/components/section/admin/AdminAuditLogFilters'
import { AdminAuditLogRow } from '@/components/ui/admin/AdminAuditLogRow'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminAuditLogs } from '@/hooks/admin/useAdminAuditLogs'

export const AdminAuditLog = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const { data, isLoading, isError } = useAdminAuditLogs()

  const urlPage = Number(searchParams.get('page')) || 1
  const currentPage = urlPage < 1 ? 1 : urlPage
  const auditLogs = data?.content ?? []
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
        {data ? `Audit log (${data.totalElements})` : 'Audit log'}
      </h1>
      <p className="mb-6 max-w-2xl text-sm text-muted-foreground">
        Every change made in the admin area, newest first. Records cannot be edited or deleted.
      </p>
      <AdminAuditLogFilters />
      {isError && <FormError message="Could not load the audit log" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && (
        <div className="overflow-x-auto border border-border bg-card">
          <table className="w-full min-w-[900px] text-sm">
            <thead>
              <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                <th className="p-4">When</th>
                <th className="p-4">Who</th>
                <th className="p-4">Action</th>
                <th className="p-4">Item</th>
                <th className="p-4">Details</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground"
                  >
                    No records found
                  </td>
                </tr>
              ) : (
                auditLogs.map((auditLog) => (
                  <AdminAuditLogRow key={auditLog.id} auditLog={auditLog} />
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
    </div>
  )
}
