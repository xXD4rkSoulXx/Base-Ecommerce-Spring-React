import { useMutation, useQueryClient } from '@tanstack/react-query'
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
      queryClient.invalidateQueries({ queryKey: ['admin', 'discount-codes'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
