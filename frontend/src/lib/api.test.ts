import { delay, http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it } from 'vitest'
import { api } from '@/lib/api'
import { hasSessionHint } from '@/lib/sessionHint'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'
import { apiUrl, server } from '@/test/server'

const initialState = useAuthStore.getInitialState()
const user = buildUser()

let refreshCalls = 0

const mockRefreshSuccess = () =>
  server.use(
    http.post(apiUrl('/api/auth/refresh'), async () => {
      refreshCalls += 1
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

const mockProtectedRoute = () =>
  server.use(
    http.get(apiUrl('/api/orders'), ({ request }) =>
      request.headers.get('authorization') === 'Bearer new-token'
        ? HttpResponse.json({ ok: true })
        : new HttpResponse(null, { status: 401 })
    )
  )

const mockHeadersEcho = () =>
  server.use(http.get(apiUrl('/api/echo'), ({ request }) => HttpResponse.json(Object.fromEntries(request.headers))))

const clearCsrfCookie = () => {
  document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT'
}

describe('api request interceptor', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
    clearCsrfCookie()
    mockHeadersEcho()
  })

  it('always sends the X-Requested-With header', async () => {
    const { data } = await api.get('/api/echo')

    expect(data['x-requested-with']).toBe('XMLHttpRequest')
  })

  it('sends the access token as a Bearer token when there is one', async () => {
    useAuthStore.getState().setAuth(user, 'my-token')

    const { data } = await api.get('/api/echo')

    expect(data.authorization).toBe('Bearer my-token')
  })

  it('does not send an Authorization header without a token', async () => {
    const { data } = await api.get('/api/echo')

    expect(data).not.toHaveProperty('authorization')
  })

  it('sends the CSRF token from the cookie', async () => {
    document.cookie = 'XSRF-TOKEN=csrf-123'

    const { data } = await api.get('/api/echo')

    expect(data['x-xsrf-token']).toBe('csrf-123')
  })

  it('does not send a CSRF header without the cookie', async () => {
    const { data } = await api.get('/api/echo')

    expect(data).not.toHaveProperty('x-xsrf-token')
  })
})

describe('api response interceptor', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
    refreshCalls = 0
  })

  it('refreshes the session on a 401 and retries the request with the new token', async () => {
    mockProtectedRoute()
    mockRefreshSuccess()

    const { data } = await api.get('/api/orders')

    expect(data).toEqual({ ok: true })
    expect(refreshCalls).toBe(1)
    expect(useAuthStore.getState().accessToken).toBe('new-token')
  })

  it('shares one refresh between simultaneous requests that got a 401', async () => {
    mockProtectedRoute()
    mockRefreshSuccess()

    const responses = await Promise.all([api.get('/api/orders'), api.get('/api/orders'), api.get('/api/orders')])

    expect(responses.map((response) => response.data)).toEqual([{ ok: true }, { ok: true }, { ok: true }])
    expect(refreshCalls).toBe(1)
  })

  it('retries only once, so a request that keeps failing does not loop', async () => {
    server.use(http.get(apiUrl('/api/orders'), () => new HttpResponse(null, { status: 401 })))
    mockRefreshSuccess()

    await expect(api.get('/api/orders')).rejects.toMatchObject({ response: { status: 401 } })

    expect(refreshCalls).toBe(1)
  })

  it('clears the session and rejects when the refresh fails', async () => {
    useAuthStore.getState().setAuth(user, 'old-token')
    mockProtectedRoute()
    mockRefreshFailure()

    await expect(api.get('/api/orders')).rejects.toMatchObject({ response: { status: 401 } })

    expect(useAuthStore.getState().accessToken).toBeNull()
    expect(hasSessionHint()).toBe(false)
  })

  it.each(['/api/auth/login', '/api/auth/register', '/api/auth/refresh', '/api/auth/logout'])(
    'does not try to refresh or retry on a 401 from %s',
    async (path) => {
      let pathCalls = 0

      mockRefreshSuccess()
      server.use(
        http.post(apiUrl(path), () => {
          pathCalls += 1

          return new HttpResponse(null, { status: 401 })
        })
      )

      await expect(api.post(path)).rejects.toMatchObject({ response: { status: 401 } })

      expect(pathCalls).toBe(1)
      expect(refreshCalls).toBe(0)
    }
  )

  it.each([400, 403, 404, 500])('does not try to refresh on a %s', async (status) => {
    server.use(http.get(apiUrl('/api/orders'), () => new HttpResponse(null, { status })))
    mockRefreshSuccess()

    await expect(api.get('/api/orders')).rejects.toMatchObject({ response: { status } })

    expect(refreshCalls).toBe(0)
  })
})
