import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminTeamService } from '@/services/admin/adminTeam.service'

export const useAdminAssignRole = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: adminTeamService.assignRole,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'team'] })
  })
}
