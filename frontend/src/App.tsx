import { useEffect } from 'react'

import AppRouter from '@/AppRouter'
import Loading from '@/pages/Loading'

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

  return <AppRouter />
}

export default App
