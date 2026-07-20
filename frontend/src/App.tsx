import { useEffect } from 'react'
import { Route, Routes } from 'react-router-dom'

import Home from '@/pages/Home'
import Layout from '@/layouts/Layout'
import ProtectedRoute from '@/layouts/ProtectedRoute'
import PublicRoute from '@/layouts/PublicRoute'
import Loading from '@/pages/Loading'
import NotFound from '@/pages/NotFound'
import NoPermission from '@/pages/NoPermission'
import Register from '@/pages/Register'
import Login from '@/pages/Login'
import Account from '@/pages/Account'
import About from '@/pages/About'

import type { LoginResponse } from '@/types/Auth'

import { api } from '@/services/api'
import { useAuthStore } from '@/store/useAuthStore'

function App() {
  const { isHydrated, setHydrated, setAuth, clearAuth } = useAuthStore();

  useEffect(() => {
    const checkSessionOnLoad = async () => {
      try {
        const response = await api.post<LoginResponse>("/api/auth/refresh", {});

        setAuth(response.data.user, response.data.accessToken);
      } catch (error) {
        console.error("Refresh session failed: ", error);
        clearAuth();
      } finally {
        setHydrated();
      }
    };

    checkSessionOnLoad();
  }, [setHydrated, setAuth, clearAuth]);

  if (!isHydrated) {
    return <Loading />;
  }

  // <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route element={<PublicRoute />}>
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
        </Route>
        <Route element={<ProtectedRoute />}>
          <Route path="/account" element={<Account />} />
        </Route>
      </Route>
      <Route path="/no-permission" element={<NoPermission />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default App
