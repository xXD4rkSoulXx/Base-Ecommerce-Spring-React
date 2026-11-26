import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, act } from '@testing-library/react'
import type { ReactNode } from 'react'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it } from 'vitest'
import { useEndSession } from '@/hooks/useEndSession'
import { queryKeys } from '@/queryKeys'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'

const initialState = useAuthStore.getInitialState()

const renderEndSession = () => {
  const queryClient = new QueryClient()
  const wrapper = ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/orders']}>{children}</MemoryRouter>
    </QueryClientProvider>
  )
  const { result } = renderHook(() => ({ endSession: useEndSession(), location: useLocation() }), { wrapper })

  return { result, queryClient }
}

describe('useEndSession', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
  })

  it('clears the authentication', () => {
    useAuthStore.getState().setAuth(buildUser(), 'token')
    const { result } = renderEndSession()

    act(() => result.current.endSession())

    expect(useAuthStore.getState().user).toBeNull()
    expect(useAuthStore.getState().accessToken).toBeNull()
  })

  it('clears the whole query cache so the next user does not see old data', () => {
    const { result, queryClient } = renderEndSession()
    queryClient.setQueryData(queryKeys.cart, { items: [] })
    queryClient.setQueryData(queryKeys.profile, { user: buildUser() })

    act(() => result.current.endSession())

    expect(queryClient.getQueryCache().getAll()).toHaveLength(0)
  })

  it('goes to the login page and replaces the history entry', () => {
    const { result } = renderEndSession()

    act(() => result.current.endSession())

    expect(result.current.location.pathname).toBe('/login')
  })
})
