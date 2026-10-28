import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/store/useAuthStore'

export const useEndSession = () => {
  const clearAuth = useAuthStore((state) => state.clearAuth)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  return () => {
    clearAuth()
    navigate('/login', { replace: true })
    queryClient.clear()
  }
}
