import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { authService } from '@/services/auth.service'
import { useAuthStore } from '@/store/useAuthStore'

export const useLogout = () => {
  const clearAuth = useAuthStore((state) => state.clearAuth)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: authService.logout,
    onSettled: () => {
      clearAuth()
      navigate('/login', { replace: true })
      queryClient.clear()
    }
  })
}
