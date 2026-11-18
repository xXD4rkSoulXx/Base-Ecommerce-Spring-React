import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { authService } from '@/services/auth.service'
import { useEndSession } from '@/hooks/useEndSession'
import type { ChangePasswordRequest } from '@/types/auth'

export const useChangePassword = () => {
  const endSession = useEndSession()

  return useMutation({
    mutationFn: (request: ChangePasswordRequest) => authService.changePassword(request),
    onSuccess: () => {
      toast.success('Password updated. Please sign in again.')
      endSession()
    }
  })
}
