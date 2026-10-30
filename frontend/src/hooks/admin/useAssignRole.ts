import { useMutation, useQueryClient } from '@tanstack/react-query'
import { teamService } from '@/services/admin/team.service'

export const useAssignRole = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: teamService.assignRole,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'team'] })
  })
}
