import { RouterProvider } from 'react-router-dom'
import { Toaster } from 'sonner'
import { router } from '@/routes'
import { Loading } from '@/pages/Loading'
import { useInitializeAuth } from '@/hooks/useInitializeAuth'

export const App = () => {
  const isHydrated = useInitializeAuth()

  if (!isHydrated) {
    return <Loading />
  }

  return (
    <>
      <RouterProvider router={router} />
      <Toaster
        position="bottom-right"
        toastOptions={{
          unstyled: true,
          classNames: {
            toast: 'flex items-center gap-3 border border-border bg-card px-4 py-3 font-mono text-xs uppercase tracking-widest text-foreground shadow-lg',
            success: 'border-neon-cyan text-neon-cyan',
            error: 'border-destructive text-destructive'
          }
        }}
      />
    </>
  )
}
