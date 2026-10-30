import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/store/useAuthStore'
import type { Permission } from '@/types/user'

interface ProtectedRouteProps {
  permission?: Permission
}

export const ProtectedRoute = ({ permission }: ProtectedRouteProps) => {
  const user = useAuthStore((state) => state.user)
  const accessToken = useAuthStore((state) => state.accessToken)
  const location = useLocation()

  if (!user || !accessToken) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (permission && !user.permissions.includes(permission)) {
    return <Navigate to="/no-permission" replace />
  }

  return <Outlet />
}
