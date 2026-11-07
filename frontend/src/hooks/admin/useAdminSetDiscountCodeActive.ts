import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminDiscountCodeService } from '@/services/admin/adminDiscountCode.service'

interface AdminSetDiscountCodeActiveVariables {
  id: number
  active: boolean
}

export const useAdminSetDiscountCodeActive = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, active }: AdminSetDiscountCodeActiveVariables) =>
      active
        ? adminDiscountCodeService.activateDiscountCode(id)
        : adminDiscountCodeService.deactivateDiscountCode(id),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'discount-codes'] })
    }
  })
}
