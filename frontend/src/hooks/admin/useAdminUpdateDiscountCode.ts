import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminDiscountCodeService } from '@/services/admin/adminDiscountCode.service'
import type { AdminUpdateDiscountCodeRequest } from '@/types/admin/adminDiscountCode'

interface AdminUpdateDiscountCodeVariables {
  id: number
  request: AdminUpdateDiscountCodeRequest
}

export const useAdminUpdateDiscountCode = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, request }: AdminUpdateDiscountCodeVariables) =>
      adminDiscountCodeService.updateDiscountCode(id, request),
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'discount-codes'] })
    }
  })
}
