import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminTeamService } from '@/services/admin/adminTeam.service'

export const useAdminAssignRole = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: adminTeamService.assignRole,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.team })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.customers.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
