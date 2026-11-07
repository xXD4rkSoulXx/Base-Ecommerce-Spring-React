import { api } from '@/lib/api'
import type { AdminAssignRoleRequest, AdminTeamMemberResponse } from '@/types/admin/adminTeam'

export const adminTeamService = {
  getTeam: async (): Promise<AdminTeamMemberResponse[]> => {
    const response = await api.get<AdminTeamMemberResponse[]>('/api/admin/team')

    return response.data
  },

  assignRole: async (request: AdminAssignRoleRequest): Promise<AdminTeamMemberResponse> => {
    const response = await api.put<AdminTeamMemberResponse>('/api/admin/team/role', request)

    return response.data
  }
}
