import { delay, http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it } from 'vitest'
import { hasSessionHint } from '@/lib/sessionHint'
import { refreshAuth } from '@/lib/refreshAuth'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'
import { apiUrl, server } from '@/test/server'

const initialState = useAuthStore.getInitialState()
const user = buildUser()

let refreshCalls = 0
let requestedWith: string | null = null

const mockRefreshSuccess = () =>
  server.use(
    http.post(apiUrl('/api/auth/refresh'), async ({ request }) => {
      refreshCalls += 1
      requestedWith = request.headers.get('x-requested-with')
      await delay(20)

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

describe('refreshAuth', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
    refreshCalls = 0
    requestedWith = null
  })

  it('stores the new session and returns the response', async () => {
    mockRefreshSuccess()

    const response = await refreshAuth()

    expect(response).toEqual({ user, accessToken: 'new-token' })
    expect(useAuthStore.getState().accessToken).toBe('new-token')
    expect(useAuthStore.getState().user).toEqual(user)
    expect(hasSessionHint()).toBe(true)
  })

  it('sends the X-Requested-With header', async () => {
    mockRefreshSuccess()

    await refreshAuth()

    expect(requestedWith).toBe('XMLHttpRequest')
  })

  it('clears the session and rethrows when the refresh fails', async () => {
    useAuthStore.getState().setAuth(user, 'old-token')
    mockRefreshFailure()

    await expect(refreshAuth()).rejects.toMatchObject({ response: { status: 401 } })

    expect(useAuthStore.getState().accessToken).toBeNull()
    expect(useAuthStore.getState().user).toBeNull()
    expect(hasSessionHint()).toBe(false)
  })

  it('shares one request between simultaneous calls', async () => {
    mockRefreshSuccess()

    const [first, second] = await Promise.all([refreshAuth(), refreshAuth()])

    expect(refreshCalls).toBe(1)
    expect(first).toBe(second)
  })

  it('makes a new request once the previous one has finished', async () => {
    mockRefreshSuccess()

    await refreshAuth()
    await refreshAuth()

    expect(refreshCalls).toBe(2)
  })

  it('allows a new attempt after a failed refresh', async () => {
    mockRefreshFailure()
    await expect(refreshAuth()).rejects.toBeDefined()

    mockRefreshSuccess()
    await refreshAuth()

    expect(refreshCalls).toBe(2)
    expect(useAuthStore.getState().accessToken).toBe('new-token')
  })
})
