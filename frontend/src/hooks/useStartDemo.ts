import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { markDemoSession } from '@/demo/demoSession'
import { DEMO_ACCESS_TOKEN, DEMO_USER } from '@/demo/data/people'
import { startDemo } from '@/demo/startDemo'
import { useAuthStore } from '@/store/useAuthStore'

export const useStartDemo = () => {
  const setAuth = useAuthStore((state) => state.setAuth)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: startDemo,
    onSuccess: () => {
      markDemoSession()
      queryClient.clear()
      setAuth(DEMO_USER, DEMO_ACCESS_TOKEN)
      navigate('/admin', { replace: true })
    }
  })
}
