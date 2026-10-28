import { useMutation } from '@tanstack/react-query'
import { authService } from '@/services/auth.service'
import { useEndSession } from '@/hooks/useEndSession'

export const useLogout = () => {
  const endSession = useEndSession()

  return useMutation({
    mutationFn: authService.logout,
    onSettled: () => endSession()
  })
}
