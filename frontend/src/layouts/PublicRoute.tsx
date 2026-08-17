import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/useAuthStore'

export const PublicRoute = () => {
  const user = useAuthStore((state) => state.user)
  const accessToken = useAuthStore((state) => state.accessToken)

  if (user && accessToken) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
