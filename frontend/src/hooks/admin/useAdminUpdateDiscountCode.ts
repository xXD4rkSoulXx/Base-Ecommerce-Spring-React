import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminDiscountCodeService } from '@/services/admin/adminDiscountCode.service'
import type { AdminUpdateDiscountCodeRequest } from '@/types/admin/adminDiscountCode'

interface UpdateDiscountCodeVariables {
  id: number
  request: AdminUpdateDiscountCodeRequest
}

export const useAdminUpdateDiscountCode = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, request }: UpdateDiscountCodeVariables) =>
      adminDiscountCodeService.updateDiscountCode(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'discount-codes'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
