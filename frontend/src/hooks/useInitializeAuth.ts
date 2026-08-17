import { useEffect } from 'react'
import axios from 'axios'
import { useAuthStore } from '@/store/useAuthStore'
import { api } from '@/lib/api'

export const useInitializeAuth = () => {
  const isHydrated = useAuthStore((state) => state.isHydrated)
  const setHydrated = useAuthStore((state) => state.setHydrated)
  const setAuth = useAuthStore((state) => state.setAuth)
  const clearAuth = useAuthStore((state) => state.clearAuth)

  useEffect(() => {
    const controller = new AbortController()

    const checkSessionOnLoad = async () => {
      try {
        const response = await api.post('/api/auth/refresh', {}, { signal: controller.signal })
        
        setAuth(response.data.user, response.data.accessToken)
      } catch (error) {
        if (axios.isCancel(error)) {
          return
        }

        clearAuth()
      } finally {
        if (!controller.signal.aborted) {
          setHydrated()
        }
      }
    }

    checkSessionOnLoad()

    return () => controller.abort()
  }, [setHydrated, setAuth, clearAuth])

  return isHydrated
}
