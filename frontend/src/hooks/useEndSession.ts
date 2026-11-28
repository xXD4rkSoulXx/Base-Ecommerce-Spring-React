import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { unmarkDemoSession } from '@/demo/demoSession'
import { stopDemo } from '@/demo/startDemo'
import { useAuthStore } from '@/store/useAuthStore'

export const useEndSession = () => {
  const clearAuth = useAuthStore((state) => state.clearAuth)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  return () => {
    stopDemo()
    unmarkDemoSession()
    clearAuth()
    queryClient.clear()
    navigate('/login', { replace: true })
  }
}
