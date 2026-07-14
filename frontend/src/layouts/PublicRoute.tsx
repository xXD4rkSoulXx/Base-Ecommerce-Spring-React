import { Navigate, Outlet } from "react-router-dom"

import { useAuthStore } from "@/store/useAuthStore"

function PublicRoute() {
  const { accessToken, user } = useAuthStore();

  if (accessToken && user) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export default PublicRoute
