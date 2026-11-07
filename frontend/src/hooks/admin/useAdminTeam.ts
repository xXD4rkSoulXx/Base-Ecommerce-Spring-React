import { useQuery } from '@tanstack/react-query'
import { adminTeamService } from '@/services/admin/adminTeam.service'

export const useAdminTeam = () =>
  useQuery({
    queryKey: ['admin', 'team'],
    queryFn: adminTeamService.getTeam
  })
