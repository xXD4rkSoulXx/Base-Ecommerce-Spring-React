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
  details: string | null
  createdAt: string
}

export const AUDIT_ACTION_LABELS: Record<AuditAction, string> = {
  CREATED: 'Created',
  UPDATED: 'Updated',
  ARCHIVED: 'Archived',
  RESTORED: 'Restored',
  ACTIVATED: 'Activated',
  DEACTIVATED: 'Deactivated',
  HIDDEN: 'Hidden',
  SHOWN: 'Shown',
  STOCK_ADJUSTED: 'Stock adjusted',
  STOCK_SET: 'Stock set',
  IMAGE_UPLOADED: 'Image uploaded',
  IMAGE_DELETED: 'Image deleted',
  COVER_CHANGED: 'Cover changed',
  ROLE_CHANGED: 'Role changed',
  STATUS_CHANGED: 'Status changed',
  SHIPMENT_STATUS_CHANGED: 'Parcel status changed',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  RECEIVED: 'Received',
  REFUNDED: 'Refunded'
}

export const AUDIT_ENTITY_TYPE_LABELS: Record<AuditEntityType, string> = {
  PRODUCT: 'Product',
  VARIANT: 'Variant',
  ORDER: 'Order',
  RETURN: 'Return',
  DISCOUNT_CODE: 'Discount code',
  REVIEW: 'Review',
  TEAM_MEMBER: 'Team member'
}
