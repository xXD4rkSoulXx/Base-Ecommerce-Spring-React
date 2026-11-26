import { renderHook, waitFor } from '@testing-library/react'
import { delay, http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { DEMO_ACCESS_TOKEN, DEMO_USER } from '@/demo/data/people'
import { isDemoSession, markDemoSession } from '@/demo/demoSession'
import { startDemo } from '@/demo/startDemo'
import { useInitializeAuth } from '@/hooks/useInitializeAuth'
import { hasSessionHint, markSessionHint } from '@/lib/sessionHint'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'
import { apiUrl, server } from '@/test/server'

vi.mock('@/demo/startDemo', () => ({ startDemo: vi.fn() }))

const initialState = useAuthStore.getInitialState()
const user = buildUser()

let refreshCalls = 0

const mockRefreshSuccess = (delayMs = 0) =>
  server.use(
    http.post(apiUrl('/api/auth/refresh'), async () => {
      refreshCalls += 1
      await delay(delayMs)

      return HttpResponse.json({ user, accessToken: 'new-token' })
    })
  )

const mockRefreshFailure = () =>
  server.use(
    http.post(apiUrl('/api/auth/refresh'), () => {
      refreshCalls += 1

      return new HttpResponse(null, { status: 401 })
    })
  )

describe('useInitializeAuth', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
    sessionStorage.clear()
    refreshCalls = 0
    vi.mocked(startDemo).mockResolvedValue()
  })

  it('is hydrated without calling the server when there is no previous session', async () => {
    const { result } = renderHook(() => useInitializeAuth())

    await waitFor(() => expect(result.current).toBe(true))

    expect(refreshCalls).toBe(0)
    expect(useAuthStore.getState().user).toBeNull()
  })

  it('restores the session from the refresh cookie when there is a session hint', async () => {
    markSessionHint()
    mockRefreshSuccess()

    const { result } = renderHook(() => useInitializeAuth())

    await waitFor(() => expect(result.current).toBe(true))

    expect(useAuthStore.getState().user).toEqual(user)
    expect(useAuthStore.getState().accessToken).toBe('new-token')
  })

  it('is not hydrated until the refresh has finished', async () => {
    markSessionHint()
    mockRefreshSuccess(50)

    const { result } = renderHook(() => useInitializeAuth())

    expect(result.current).toBe(false)

    await waitFor(() => expect(result.current).toBe(true))
  })

  it('ends up as a visitor and drops the hint when the refresh fails', async () => {
    markSessionHint()
    mockRefreshFailure()

    const { result } = renderHook(() => useInitializeAuth())

    await waitFor(() => expect(result.current).toBe(true))

    expect(useAuthStore.getState().user).toBeNull()
    expect(hasSessionHint()).toBe(false)
  })

  it('starts the demo and signs in as the demo user in a demo session', async () => {
    markDemoSession()

    const { result } = renderHook(() => useInitializeAuth())

    await waitFor(() => expect(result.current).toBe(true))

    expect(startDemo).toHaveBeenCalledTimes(1)
    expect(useAuthStore.getState().user).toEqual(DEMO_USER)
    expect(useAuthStore.getState().accessToken).toBe(DEMO_ACCESS_TOKEN)
    expect(refreshCalls).toBe(0)
  })

  it('leaves the demo session and falls back to the normal flow when the demo cannot start', async () => {
    markDemoSession()
    markSessionHint()
    mockRefreshSuccess()
    vi.mocked(startDemo).mockRejectedValue(new Error('worker failed'))

    const { result } = renderHook(() => useInitializeAuth())

    await waitFor(() => expect(result.current).toBe(true))

    expect(isDemoSession()).toBe(false)
    expect(useAuthStore.getState().user).toEqual(user)
  })

  it('does not mark the app as hydrated when it unmounts before the initialization finishes', async () => {
    markSessionHint()
    mockRefreshSuccess(30)

    const { unmount } = renderHook(() => useInitializeAuth())

    unmount()
    await new Promise((resolve) => setTimeout(resolve, 80))

    expect(useAuthStore.getState().isHydrated).toBe(false)
  })
})
