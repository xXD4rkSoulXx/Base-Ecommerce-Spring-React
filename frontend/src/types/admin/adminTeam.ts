import type { UserRole } from '@/types/user'

export interface AdminAssignRoleRequest {
  email: string
  role: UserRole
}

export interface AdminTeamMemberResponse {
  id: number
  email: string
  firstName: string
  lastName: string
  role: UserRole
  createdAt: string
}
