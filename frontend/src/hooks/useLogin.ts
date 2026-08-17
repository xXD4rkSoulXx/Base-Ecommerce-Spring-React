import { useMutation } from '@tanstack/react-query'
import { useLocation, useNavigate } from 'react-router-dom'
import { authService } from '@/services/auth.service'
import { useAuthStore } from '@/store/useAuthStore'

interface LocationState {
  from?: {
    pathname: string
  }
}

export const useLogin = () => {
  const setAuth = useAuthStore((state) => state.setAuth)
  const navigate = useNavigate()
  const location = useLocation()

  return useMutation({
    mutationFn: authService.login,
    onSuccess: (data) => {
      const state = location.state as LocationState
      const from = state?.from?.pathname || '/'

      setAuth(data.user, data.accessToken)
      navigate(from, { replace: true })
    }
  })
}
