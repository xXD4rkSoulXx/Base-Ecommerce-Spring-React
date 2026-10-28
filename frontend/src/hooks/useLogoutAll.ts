import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { authService } from '@/services/auth.service'
import { useEndSession } from '@/hooks/useEndSession'

export const useLogoutAll = () => {
  const endSession = useEndSession()

  return useMutation({
    mutationFn: authService.logoutAll,
    onSuccess: () => {
      toast.success('All sessions terminated')
      endSession()
    }
  })
}
