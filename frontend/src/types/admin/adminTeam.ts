import type { Schema } from '@/types/schema'
import type { UserRole } from '@/types/user'

export interface AdminAssignRoleRequest {
  email: string
  role: UserRole
}

export interface AdminTeamMemberResponse extends Omit<Schema<'AdminTeamMemberResponse'>, 'role'> {
  role: UserRole
}
