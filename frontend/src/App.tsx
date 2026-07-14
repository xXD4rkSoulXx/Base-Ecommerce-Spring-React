import { useEffect } from "react"
import { Route, Routes } from "react-router-dom"

import Home from "@/pages/Home"
import Layout from "@/layouts/Layout"
// import ProtectedRoute from "@/layouts/ProtectedRoute"
import PublicRoute from "@/layouts/PublicRoute"
import Loading from "@/pages/Loading"
import NotFound from "@/pages/NotFound"
import NoPermission from "@/pages/NoPermission"
import Register from "@/pages/Register"
import Login from "@/pages/Login"
import Account from "./pages/Account"
import About from "@/pages/About"
import { useAuthStore } from "@/store/useAuthStore"
import type { LoginResponse } from "./types/Auth"
import axios from "axios"
import ProtectedRoute from "./layouts/ProtectedRoute"

function App() {
  const { isHydrated, setAuth, clearAuth, setHydrated } = useAuthStore();

  useEffect(() => {
    const checkSessionOnLoad = async () => {
      try {
        const response = await axios.post<LoginResponse>(
          `${import.meta.env.VITE_API_URL}/api/auth/refresh`,
          {},
          { withCredentials: true }
        );

        if (response.status === 204 || !response.data) {
          clearAuth();
        } else {
          setAuth(response.data.accessToken, response.data.user);
        }
      } catch {
        clearAuth();
      } finally {
        setHydrated();
      }
    };

    checkSessionOnLoad();
  }, [setAuth, clearAuth, setHydrated]);

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
