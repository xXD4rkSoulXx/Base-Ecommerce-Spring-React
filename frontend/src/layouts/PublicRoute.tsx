import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/store/useAuthStore'

interface LocationState {
  from?: {
    pathname: string
  }
}

export const PublicRoute = () => {
  const user = useAuthStore((state) => state.user)
  const accessToken = useAuthStore((state) => state.accessToken)
  const location = useLocation()

  if (user && accessToken) {
    const from = (location.state as LocationState | null)?.from?.pathname || '/'

    return <Navigate to={from} replace />
  }

  return <Outlet />
}
