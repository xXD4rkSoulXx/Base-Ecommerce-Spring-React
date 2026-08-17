import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { authService } from '@/services/auth.service'

export const useRegister = () => {
  const navigate = useNavigate()

  return useMutation({
    mutationFn: authService.register,
    onSuccess: () => {
      navigate('/login', { replace: true })
    }
  })
}
