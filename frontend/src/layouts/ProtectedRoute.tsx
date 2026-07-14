import { Navigate, Outlet, useLocation } from "react-router-dom"

import { useAuthStore } from "@/store/useAuthStore"
import type { UserRole } from "@/types/User"

interface ProtectedRouteProps {
  allowedRoles?: UserRole[];
}

function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { accessToken, user } = useAuthStore();
  const location = useLocation();

  if (!accessToken || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/no-permission" replace />;
  }

  return <Outlet />;
}

export default ProtectedRoute
