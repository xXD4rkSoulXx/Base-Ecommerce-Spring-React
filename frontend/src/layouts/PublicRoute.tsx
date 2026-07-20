import { Navigate, Outlet } from 'react-router-dom'

import { useAuthStore } from '@/store/useAuthStore'

function PublicRoute() {
  const { user, accessToken } = useAuthStore();

  if (user && accessToken) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export default PublicRoute
