import type { AuditAction, AuditEntityType } from '@/types/admin/adminAuditLog'
import type { ProductCategory, ProductSort } from '@/types/product'
import type { OrderStatus, ReturnReason, ReturnStatus, ShipmentStatus } from '@/types/order'

export const APP_NAME = 'VELO_CITI'
export const MAIN_CONTENT_ID = 'main-content'

export const LEGAL_LAST_UPDATED = 'November 22, 2026'

export const MAX_CART_ITEM_QUANTITY = 10
export const MAX_CART_LINES = 20

export const TODO_FILTER = 'TODO'
export const ALL_FILTER = 'ALL'
export const DEFAULT_SORT: ProductSort = 'recent'
export const LIST_PAGE_SIZE = 10
export const PRODUCT_PAGE_SIZE = 12

export const DASHBOARD_PERIODS = [7, 30, 90]
export const DEFAULT_DASHBOARD_PERIOD = 30

export const PRODUCT_CATEGORIES: ProductCategory[] = [
  'OUTERWEAR',
  'TOPS',
  'BOTTOMS',
  'FOOTWEAR',
  'UNDERWEAR',
  'ACCESSORIES'
]

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  PAID: 'Paid',
  PROCESSING: 'Processing',
  SHIPPED: 'Shipped',
  DELIVERED: 'Delivered',
  RETURN_REQUESTED: 'Return Requested',
  REFUNDED: 'Refunded',
  CANCELED_REFUNDED: 'Canceled'
}

export const SHIPMENT_STATUS_LABELS: Record<ShipmentStatus, string> = {
  PRE_TRANSIT: 'Pre-transit',
  IN_TRANSIT: 'In transit',
  OUT_FOR_DELIVERY: 'Out for delivery',
  DELIVERED: 'Delivered',
  FAILURE: 'Delivery failed'
}

export const RETURN_STATUS_LABELS: Record<ReturnStatus, string> = {
  REQUESTED: 'Requested',
  APPROVED: 'Approved',
  RECEIVED: 'Received',
  REFUNDED: 'Refunded',
  REJECTED: 'Rejected'
}

export const RETURN_REASON_LABELS: Record<ReturnReason, string> = {
  DOES_NOT_FIT: 'Does not fit',
  DEFECTIVE: 'Defective or damaged',
  NOT_AS_DESCRIBED: 'Not as described',
  CHANGED_MIND: 'Changed my mind',
  OTHER: 'Other'
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

export const SHIPPING_COUNTRY_CODES = [
  'AO',
  'AR',
  'AU',
  'AT',
  'BE',
  'BR',
  'CA',
  'CV',
  'CL',
  'CN',
  'CO',
  'CR',
  'CU',
  'CZ',
  'DK',
  'EC',
  'EG',
  'FI',
  'FR',
  'DE',
  'GR',
  'HU',
  'IN',
  'ID',
  'IE',
  'IL',
  'IT',
  'JP',
  'KE',
  'MY',
  'MX',
  'MA',
  'MZ',
  'NL',
  'NZ',
  'NG',
  'NO',
  'PA',
  'PE',
  'PH',
  'PL',
  'PT',
  'QA',
  'RO',
  'RU',
  'SA',
  'SG',
  'ZA',
  'KR',
  'ES',
  'SE',
  'CH',
  'TH',
  'TR',
  'UA',
  'AE',
  'GB',
  'US',
  'UY',
  'VE',
  'VN'
]
