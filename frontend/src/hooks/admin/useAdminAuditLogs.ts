import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { usePageParams } from '@/hooks/usePageParams'
import { queryKeys } from '@/queryKeys'
import { adminAuditLogService } from '@/services/admin/adminAuditLog.service'
import type { AuditAction, AuditEntityType } from '@/types/admin/adminAuditLog'

export const useAdminAuditLogs = () => {
  const [searchParams] = useSearchParams()
  const { page, size } = usePageParams(20)
  const q = searchParams.get('q') || ''
  const entityType = (searchParams.get('entityType') as AuditEntityType | null) || undefined
  const action = (searchParams.get('action') as AuditAction | null) || undefined

  return useQuery({
    queryKey: queryKeys.admin.auditLogs({ q, entityType, action, page, size }),
    queryFn: () =>
      adminAuditLogService.getAuditLogs({
        q: q || undefined,
        entityType,
        action,
        page: page - 1,
        size
      }),
    placeholderData: (previousData) => previousData
  })
}
