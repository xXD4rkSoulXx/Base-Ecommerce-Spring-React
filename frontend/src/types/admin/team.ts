import type { UserRole } from '@/types/user'

export interface AssignRoleRequest {
  email: string
  role: UserRole
}

export interface TeamMemberResponse {
  id: number
  email: string
  firstName: string
  lastName: string
  role: UserRole
  createdAt: string
}
