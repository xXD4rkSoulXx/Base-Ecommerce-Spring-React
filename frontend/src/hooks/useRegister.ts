import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { authService } from '@/services/auth.service'
import type { RegisterRequest } from '@/types/auth'

export const useRegister = () => {
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (request: RegisterRequest) => authService.register(request),
    onSuccess: () => {
      toast.success('Account created. Please sign in.')
      navigate('/login', { replace: true })
    }
  })
}
