import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { adminAuditLogService } from '@/services/admin/adminAuditLog.service'
import type { AuditAction, AuditEntityType } from '@/types/admin/adminAuditLog'

export const useAdminAuditLogs = () => {
  const [searchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const entityType = (searchParams.get('entityType') as AuditEntityType | null) || undefined
  const action = (searchParams.get('action') as AuditAction | null) || undefined
  const urlPage = Number(searchParams.get('page')) || 1
  const size = Number(searchParams.get('size')) || 20
  const displayPage = urlPage < 1 ? 1 : urlPage

  return useQuery({
    queryKey: [...['admin', 'audit-logs'], { q, entityType, action, page: displayPage, size }],
    queryFn: () => adminAuditLogService.getAuditLogs({
      q: q || undefined,
      entityType,
      action,
      page: displayPage - 1,
      size
    }),
    placeholderData: (previousData) => previousData
  })
}
