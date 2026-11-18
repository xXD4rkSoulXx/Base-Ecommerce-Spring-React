import { useMutation, useQueryClient } from '@tanstack/react-query'
import { adminDiscountCodeService } from '@/services/admin/adminDiscountCode.service'
import type { AdminCreateDiscountCodeRequest } from '@/types/admin/adminDiscountCode'

export const useAdminCreateDiscountCode = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: AdminCreateDiscountCodeRequest) => adminDiscountCodeService.createDiscountCode(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'discount-codes'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
    }
  })
}
