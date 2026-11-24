import type { AdminAuditLogResponse } from '@/types/admin/adminAuditLog'
import type { Schema } from '@/types/schema'

export type AdminDashboardSummaryResponse = Schema<'AdminDashboardSummaryResponse'>

export type AdminDashboardSalesPointResponse = Schema<'AdminDashboardSalesPointResponse'>

export type AdminDashboardCountResponse = Schema<'AdminDashboardCountResponse'>

export type AdminDashboardStatusCountResponse = Schema<'AdminDashboardStatusCountResponse'>

export type AdminDashboardCategoryUnitsResponse = Schema<'AdminDashboardCategoryUnitsResponse'>

export type AdminDashboardTopProductResponse = Schema<'AdminDashboardTopProductResponse'>

export type AdminDashboardDiscountCodeResponse = Schema<'AdminDashboardDiscountCodeResponse'>

export type AdminDashboardRecentOrderResponse = Schema<'AdminDashboardRecentOrderResponse'>

export type AdminDashboardReturnResponse = Schema<'AdminDashboardReturnResponse'>

export type AdminDashboardLowStockResponse = Schema<'AdminDashboardLowStockResponse'>

export type AdminDashboardCustomersResponse = Schema<'AdminDashboardCustomersResponse'>

export type AdminDashboardReviewsResponse = Schema<'AdminDashboardReviewsResponse'>

export interface AdminDashboardEndpoints {
  summary: AdminDashboardSummaryResponse
  sales: AdminDashboardSalesPointResponse[]
  'orders-by-status': AdminDashboardStatusCountResponse[]
  'sales-by-category': AdminDashboardCategoryUnitsResponse[]
  'recent-orders': AdminDashboardRecentOrderResponse[]
  'top-products': AdminDashboardTopProductResponse[]
  'top-discount-codes': AdminDashboardDiscountCodeResponse[]
  'pending-orders': AdminDashboardCountResponse
  'pending-returns': AdminDashboardCountResponse
  'low-stock': AdminDashboardCountResponse
  'returns-to-handle': AdminDashboardReturnResponse[]
  'lowest-stock': AdminDashboardLowStockResponse[]
  customers: AdminDashboardCustomersResponse
  reviews: AdminDashboardReviewsResponse
  'recent-activity': AdminAuditLogResponse[]
}
