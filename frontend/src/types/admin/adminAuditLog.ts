import type { Schema } from '@/types/schema'

export type AuditAction = Schema<'AdminAuditLogResponse'>['action']

export type AuditEntityType = Schema<'AdminAuditLogResponse'>['entityType']

export interface AdminAuditLogSearchRequest {
  q?: string
  entityType?: AuditEntityType
  action?: AuditAction
  page: number
  size: number
}

export type AdminAuditLogResponse = Schema<'AdminAuditLogResponse'>
