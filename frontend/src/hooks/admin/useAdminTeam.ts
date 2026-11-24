import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminTeamService } from '@/services/admin/adminTeam.service'

export const useAdminTeam = () =>
  useQuery({
    queryKey: queryKeys.admin.team,
    queryFn: adminTeamService.getTeam
  })
