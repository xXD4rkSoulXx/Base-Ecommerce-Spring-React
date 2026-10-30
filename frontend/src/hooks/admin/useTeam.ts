import { useQuery } from '@tanstack/react-query'
import { teamService } from '@/services/admin/team.service'

export const useTeam = () =>
  useQuery({
    queryKey: ['admin', 'team'],
    queryFn: teamService.getTeam
  })
