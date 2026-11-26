import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import type { ReactNode } from 'react'
import { beforeEach, describe, expect, it } from 'vitest'
import { useUpdateProfile } from '@/hooks/useUpdateProfile'
import { queryKeys } from '@/queryKeys'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'
import { apiUrl, server } from '@/test/server'
import type { ProfileResponse } from '@/types/user'

const initialState = useAuthStore.getInitialState()
const updatedUser = buildUser({ firstName: 'Maria' })

const renderUpdateProfile = () => {
  const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  const wrapper = ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  )

  return { ...renderHook(() => useUpdateProfile(), { wrapper }), queryClient }
}

const mockUpdate = () => server.use(http.patch(apiUrl('/api/users/me'), () => HttpResponse.json(updatedUser)))

describe('useUpdateProfile', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
    mockUpdate()
  })

  it('updates the user in the auth store', async () => {
    useAuthStore.getState().setAuth(buildUser(), 'token')
    const { result } = renderUpdateProfile()

    await result.current.mutateAsync({ firstName: 'Maria', lastName: 'Costa' })

    expect(useAuthStore.getState().user).toEqual(updatedUser)
  })

  it('updates the user inside the cached profile and keeps the other fields', async () => {
    const { result, queryClient } = renderUpdateProfile()
    queryClient.setQueryData(queryKeys.profile, { user: buildUser(), addresses: ['a'] })

    await result.current.mutateAsync({ firstName: 'Maria', lastName: 'Costa' })

    await waitFor(() =>
      expect(queryClient.getQueryData<ProfileResponse>(queryKeys.profile)).toEqual({
        user: updatedUser,
        addresses: ['a']
      })
    )
  })

  it('does not create a profile in the cache when there was none', async () => {
    const { result, queryClient } = renderUpdateProfile()

    await result.current.mutateAsync({ firstName: 'Maria', lastName: 'Costa' })

    expect(queryClient.getQueryData(queryKeys.profile)).toBeUndefined()
  })
})
