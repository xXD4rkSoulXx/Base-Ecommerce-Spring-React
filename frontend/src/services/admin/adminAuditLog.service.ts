import { api } from '@/lib/api'
import type { AdminAuditLogResponse, AdminAuditLogSearchRequest } from '@/types/admin/adminAuditLog'
import type { PageResponse } from '@/types/page'

export const adminAuditLogService = {
  getAuditLogs: async (request: AdminAuditLogSearchRequest) => {
    const response = await api.get<PageResponse<AdminAuditLogResponse>>('/api/admin/audit-logs', {
      params: request
    })

    return response.data
  }
}
