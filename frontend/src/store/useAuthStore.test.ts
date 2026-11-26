import { beforeEach, describe, expect, it } from 'vitest'
import { hasSessionHint } from '@/lib/sessionHint'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'

const initialState = useAuthStore.getInitialState()

beforeEach(() => {
  localStorage.clear()
  useAuthStore.setState(initialState, true)
})

describe('useAuthStore', () => {
  it('starts without a session and not hydrated', () => {
    const { user, accessToken, isHydrated } = useAuthStore.getState()

    expect(user).toBeNull()
    expect(accessToken).toBeNull()
    expect(isHydrated).toBe(false)
  })

  it('setAuth keeps the user and the token and marks the session hint', () => {
    const user = buildUser()

    useAuthStore.getState().setAuth(user, 'access-token')

    expect(useAuthStore.getState().user).toEqual(user)
    expect(useAuthStore.getState().accessToken).toBe('access-token')
    expect(hasSessionHint()).toBe(true)
  })

  it('setUser replaces the user and keeps the token', () => {
    useAuthStore.getState().setAuth(buildUser(), 'access-token')

    useAuthStore.getState().setUser(buildUser({ firstName: 'Rui' }))

    expect(useAuthStore.getState().user?.firstName).toBe('Rui')
    expect(useAuthStore.getState().accessToken).toBe('access-token')
  })

  it('clearAuth removes the user, the token and the session hint', () => {
    useAuthStore.getState().setAuth(buildUser(), 'access-token')

    useAuthStore.getState().clearAuth()

    expect(useAuthStore.getState().user).toBeNull()
    expect(useAuthStore.getState().accessToken).toBeNull()
    expect(hasSessionHint()).toBe(false)
  })

  it('clearAuth keeps the hydrated flag', () => {
    useAuthStore.getState().setHydrated()

    useAuthStore.getState().clearAuth()

    expect(useAuthStore.getState().isHydrated).toBe(true)
  })

  it('setHydrated marks the store as hydrated', () => {
    useAuthStore.getState().setHydrated()

    expect(useAuthStore.getState().isHydrated).toBe(true)
  })
})
