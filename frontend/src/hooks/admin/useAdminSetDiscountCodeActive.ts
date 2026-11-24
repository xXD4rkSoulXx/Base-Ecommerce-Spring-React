import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminDiscountCodeService } from '@/services/admin/adminDiscountCode.service'

interface SetDiscountCodeActiveVariables {
  id: number
  active: boolean
}

export const useAdminSetDiscountCodeActive = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, active }: SetDiscountCodeActiveVariables) =>
      active ? adminDiscountCodeService.activateDiscountCode(id) : adminDiscountCodeService.deactivateDiscountCode(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.discountCodes.all })
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.dashboard.all })
    }
  })
}
