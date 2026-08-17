import { RouterProvider } from 'react-router-dom'
import { router } from '@/routes'
import { useInitializeAuth } from '@/hooks/useInitializeAuth'
import { Loading } from '@/pages/Loading'

export const App = () => {
  const isHydrated = useInitializeAuth()

  if (!isHydrated) {
    return <Loading />
  }

  return <RouterProvider router={router} />
}
