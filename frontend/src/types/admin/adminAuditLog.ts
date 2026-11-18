export type AuditAction =
  | 'CREATED'
  | 'UPDATED'
  | 'ARCHIVED'
  | 'RESTORED'
  | 'ACTIVATED'
  | 'DEACTIVATED'
  | 'HIDDEN'
  | 'SHOWN'
  | 'STOCK_ADJUSTED'
  | 'STOCK_SET'
  | 'IMAGE_UPLOADED'
  | 'IMAGE_DELETED'
  | 'COVER_CHANGED'
  | 'ROLE_CHANGED'
  | 'STATUS_CHANGED'
  | 'SHIPMENT_STATUS_CHANGED'
  | 'APPROVED'
  | 'REJECTED'
  | 'RECEIVED'
  | 'REFUNDED'

export type AuditEntityType =
  | 'PRODUCT'
  | 'VARIANT'
  | 'ORDER'
  | 'RETURN'
  | 'DISCOUNT_CODE'
  | 'REVIEW'
  | 'TEAM_MEMBER'

export interface AdminAuditLogSearchRequest {
  q?: string
  entityType?: AuditEntityType
  action?: AuditAction
  page: number
  size: number
}

export interface AdminAuditLogResponse {
  id: number
  actorEmail: string
  action: AuditAction
  entityType: AuditEntityType
  entityId: number
  entityLabel: string
  details?: string
  createdAt: string
}
