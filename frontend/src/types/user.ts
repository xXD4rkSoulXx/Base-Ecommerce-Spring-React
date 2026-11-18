export type UserRole = 'CUSTOMER' | 'ADMIN' | 'MANAGER' | 'MERCHANDISER' | 'FULFILLMENT' | 'SUPPORT' | 'DEMO'

export type Permission =
  | 'DASHBOARD_READ'
  | 'PRODUCTS_READ'
  | 'PRODUCTS_WRITE'
  | 'STOCK_READ'
  | 'STOCK_WRITE'
  | 'ORDERS_READ'
  | 'ORDERS_WRITE'
  | 'RETURNS_READ'
  | 'RETURNS_WRITE'
  | 'DISCOUNTS_READ'
  | 'DISCOUNTS_WRITE'
  | 'CUSTOMERS_READ'
  | 'REVIEWS_READ'
  | 'REVIEWS_WRITE'
  | 'TEAM_READ'
  | 'TEAM_WRITE'
  | 'AUDIT_READ'

export interface UpdateProfileRequest {
  firstName: string
  lastName: string
}

export interface UserResponse {
  email: string
  firstName: string
  lastName: string
  role: UserRole
  permissions: Permission[]
}

export interface ProfileResponse {
  user: UserResponse
  createdAt: string
  totalOrders: number
}
