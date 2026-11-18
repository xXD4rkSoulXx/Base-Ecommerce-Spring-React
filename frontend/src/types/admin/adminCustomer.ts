export interface AdminCustomerSearchRequest {
  q?: string
  page: number
  size: number
}

export interface AdminCustomerOrderSearchRequest {
  page: number
  size: number
}

export interface AdminCustomerResponse {
  id: number
  email: string
  firstName: string
  lastName: string
  createdAt: string
  orders: number
  lastOrderAt?: string
}
