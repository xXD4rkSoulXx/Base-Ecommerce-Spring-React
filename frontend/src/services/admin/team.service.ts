import { api } from '@/lib/api'
import type { AssignRoleRequest, TeamMemberResponse } from '@/types/admin/team'

export const teamService = {
  getTeam: async (): Promise<TeamMemberResponse[]> => {
    const response = await api.get<TeamMemberResponse[]>('/api/admin/team')

    return response.data
  },

  assignRole: async (request: AssignRoleRequest): Promise<TeamMemberResponse> => {
    const response = await api.put<TeamMemberResponse>('/api/admin/team/role', request)

    return response.data
  }
}
