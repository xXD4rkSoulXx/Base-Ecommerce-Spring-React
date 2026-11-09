import type { AdminAuditLogResponse } from '@/types/admin/adminAuditLog'

export interface AdminDashboardSummaryResponse {
  netRevenue: number
  previousNetRevenue: number
  refunded: number
  previousRefunded: number
  orders: number
  previousOrders: number
  averageOrderValue: number
  previousAverageOrderValue: number
}

export interface AdminDashboardSalesPointResponse {
  date: string
  sales: number
  refunds: number
  orders: number
}

export interface AdminDashboardCountResponse {
  count: number
}

export interface AdminDashboardStatusCountResponse {
  status: string
  count: number
}

export interface AdminDashboardCategoryUnitsResponse {
  category: string
  unitsSold: number
}

export interface AdminDashboardTopProductResponse {
  productId: number
  ref: string
  name: string
  cover: string | null
  unitsSold: number
}

export interface AdminDashboardDiscountCodeResponse {
  code: string
  orders: number
}

export interface AdminDashboardRecentOrderResponse {
  id: number
  customerName: string
  status: string
  totalAmount: number
  createdAt: string
}

export interface AdminDashboardReturnResponse {
  id: number
  orderId: number
  customerName: string
  status: string
  createdAt: string
}

export interface AdminDashboardLowStockResponse {
  skuId: number
  productId: number
  productName: string
  ref: string
  size: string
  color: string
  stock: number
}

export interface AdminDashboardCustomersResponse {
  newCustomers: number
  previousNewCustomers: number
  totalCustomers: number
}

export interface AdminDashboardReviewsResponse {
  newReviews: number
  previousNewReviews: number
  averageRating: number | null
  previousAverageRating: number | null
}

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
