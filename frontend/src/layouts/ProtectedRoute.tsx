import { Navigate, Outlet, useLocation } from 'react-router-dom'

import type { UserRole } from '@/types/User'

import { useAuthStore } from '@/store/useAuthStore'

interface ProtectedRouteProps {
  allowedRoles?: UserRole[];
}

function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { user, accessToken } = useAuthStore();
  const location = useLocation();

  if (!user || !accessToken) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/no-permission" replace />;
  }

  return <Outlet />;
}

export default ProtectedRoute
