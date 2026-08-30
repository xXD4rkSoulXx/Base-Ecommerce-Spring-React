import { useEffect } from 'react'
import { refreshAuth } from '@/lib/refreshAuth'
import { useAuthStore } from '@/store/useAuthStore'

export const useInitializeAuth = () => {
  const isHydrated = useAuthStore((state) => state.isHydrated)
  const setHydrated = useAuthStore((state) => state.setHydrated)

  useEffect(() => {
    let cancelled = false

    refreshAuth()
      .catch(() => {})
      .finally(() => {
        if (!cancelled) {
          setHydrated()
        }
      })

    return () => {
      cancelled = true
    }
  }, [setHydrated])

  return isHydrated
}
