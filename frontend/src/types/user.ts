export type UserRole = 'CUSTOMER' | 'OPERATOR' | 'MANAGER' | 'ADMIN'

export interface UserResponse {
  email: string
  firstName: string
  lastName: string
  role: UserRole
}

export interface UpdateProfileRequest {
  firstName: string
  lastName: string
}

export interface ProfileResponse {
  user: UserResponse
  createdAt: string
  totalOrders: number
}
