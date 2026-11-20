import { useEffect } from 'react'
import { isDemoSession, unmarkDemoSession } from '@/demo/demoSession'
import { DEMO_ACCESS_TOKEN, DEMO_USER } from '@/demo/data/people'
import { startDemo } from '@/demo/startDemo'
import { refreshAuth } from '@/lib/refreshAuth'
import { hasSessionHint } from '@/lib/sessionHint'
import { useAuthStore } from '@/store/useAuthStore'

export const useInitializeAuth = () => {
  const isHydrated = useAuthStore((state) => state.isHydrated)
  const setAuth = useAuthStore((state) => state.setAuth)
  const setHydrated = useAuthStore((state) => state.setHydrated)

  useEffect(() => {
    let cancelled = false

    const initialize = async () => {
      if (isDemoSession()) {
        try {
          await startDemo()
          setAuth(DEMO_USER, DEMO_ACCESS_TOKEN)

          return
        } catch {
          unmarkDemoSession()
        }
      }

      if (hasSessionHint()) {
        await refreshAuth().catch(() => {})
      }
    }

    initialize().finally(() => {
      if (!cancelled) {
        setHydrated()
      }
    })

    return () => {
      cancelled = true
    }
  }, [setAuth, setHydrated])

  return isHydrated
}
