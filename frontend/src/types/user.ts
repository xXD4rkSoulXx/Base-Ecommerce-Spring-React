export type UserRole = 'CUSTOMER' | 'OPERATOR' | 'MANAGER' | 'ADMIN'

export interface UserResponse {
  email: string
  firstName: string
  lastName: string
  role: UserRole
}
