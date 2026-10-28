import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { authService } from '@/services/auth.service'
import { useEndSession } from '@/hooks/useEndSession'

export const useChangePassword = () => {
  const endSession = useEndSession()

  return useMutation({
    mutationFn: authService.changePassword,
    onSuccess: () => {
      toast.success('Password updated. Please sign in again.')
      endSession()
    }
  })
}
