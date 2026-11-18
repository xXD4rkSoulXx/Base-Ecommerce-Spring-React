import type { AdminAuditLogResponse } from '@/types/admin/adminAuditLog'
import type { OrderStatus, ReturnStatus } from '@/types/order'
import type { ProductCategory, ProductColor, ProductSize } from '@/types/product'

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
  status: OrderStatus
  count: number
}

export interface AdminDashboardCategoryUnitsResponse {
  category: ProductCategory
  unitsSold: number
}

export interface AdminDashboardTopProductResponse {
  productId: number
  ref: string
  name: string
  cover?: string
  unitsSold: number
}

export interface AdminDashboardDiscountCodeResponse {
  code: string
  orders: number
}

export interface AdminDashboardRecentOrderResponse {
  id: number
  customerName: string
  status: OrderStatus
  totalAmount: number
  createdAt: string
}

export interface AdminDashboardReturnResponse {
  id: number
  orderId: number
  customerName: string
  status: ReturnStatus
  createdAt: string
}

export interface AdminDashboardLowStockResponse {
  skuId: number
  productId: number
  productName: string
  ref: string
  size: ProductSize
  color: ProductColor
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
  averageRating?: number
  previousAverageRating?: number
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
