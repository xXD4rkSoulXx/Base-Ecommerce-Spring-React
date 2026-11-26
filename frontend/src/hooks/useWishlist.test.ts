import { act, renderHook, waitFor } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it } from 'vitest'
import { useWishlist } from '@/hooks/useWishlist'
import { useAuthStore } from '@/store/useAuthStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { buildProduct, buildUser } from '@/test/builders'
import { createWrapper } from '@/test/render'
import { apiUrl, server } from '@/test/server'

const initialAuthState = useAuthStore.getInitialState()
const initialWishlistState = useWishlistStore.getInitialState()

interface Call {
  method: string
  path: string
}

let calls: Call[]

const productA = buildProduct({ id: 1 })
const productB = buildProduct({ id: 2 })

const mockWishlistApi = (products: object[] = [productA]) =>
  server.use(
    http.all(`${apiUrl('/api/wishlist')}*`, ({ request }) => {
      if (request.method !== 'GET') {
        calls.push({ method: request.method, path: new URL(request.url).pathname })
      }

      return request.method === 'GET' ? HttpResponse.json(products) : new HttpResponse(null, { status: 204 })
    })
  )

const renderWishlist = () => renderHook(() => useWishlist(), { wrapper: createWrapper() })

describe('useWishlist as a guest', () => {
  beforeEach(() => {
    useAuthStore.setState(initialAuthState, true)
    useWishlistStore.setState(initialWishlistState, true)
    localStorage.clear()
    calls = []
  })

  it('reads the guest wishlist', () => {
    useWishlistStore.setState({ items: [productA, productB] })

    const { result } = renderWishlist()

    expect(result.current.items).toHaveLength(2)
    expect(result.current.totalItems).toBe(2)
    expect(result.current.isInWishlist(1)).toBe(true)
    expect(result.current.isInWishlist(3)).toBe(false)
  })

  it('adds a product on the first toggle and removes it on the second', () => {
    const { result } = renderWishlist()

    act(() => result.current.toggleWishlist(productA))
    expect(useWishlistStore.getState().items.map((item) => item.id)).toEqual([1])
    expect(result.current.isInWishlist(1)).toBe(true)

    act(() => result.current.toggleWishlist(productA))
    expect(useWishlistStore.getState().items).toEqual([])
  })

  it('clears the local wishlist', () => {
    useWishlistStore.setState({ items: [productA, productB] })
    const { result } = renderWishlist()

    act(() => result.current.clearWishlist())

    expect(useWishlistStore.getState().items).toEqual([])
  })
})

describe('useWishlist when signed in', () => {
  beforeEach(() => {
    useAuthStore.setState(initialAuthState, true)
    useWishlistStore.setState(initialWishlistState, true)
    localStorage.clear()
    calls = []
    useAuthStore.getState().setAuth(buildUser(), 'token')
  })

  it('loads the wishlist from the server and ignores the guest one', async () => {
    useWishlistStore.setState({ items: [productB] })
    mockWishlistApi([productA])

    const { result } = renderWishlist()

    await waitFor(() => expect(result.current.items).toHaveLength(1))

    expect(result.current.items[0].id).toBe(1)
    expect(result.current.isInWishlist(2)).toBe(false)
  })

  it('adds a product that is not in the wishlist on the server', async () => {
    mockWishlistApi([productA])
    const { result } = renderWishlist()

    await waitFor(() => expect(result.current.totalItems).toBe(1))
    act(() => result.current.toggleWishlist(productB))

    await waitFor(() => expect(calls).toEqual([{ method: 'POST', path: '/api/wishlist/2' }]))
    expect(useWishlistStore.getState().items).toEqual([])
  })

  it('removes a product that is already in the wishlist on the server', async () => {
    mockWishlistApi([productA])
    const { result } = renderWishlist()

    await waitFor(() => expect(result.current.totalItems).toBe(1))
    act(() => result.current.toggleWishlist(productA))

    await waitFor(() => expect(calls).toEqual([{ method: 'DELETE', path: '/api/wishlist/1' }]))
  })

  it('clears the wishlist on the server', async () => {
    mockWishlistApi()
    const { result } = renderWishlist()

    act(() => result.current.clearWishlist())

    await waitFor(() => expect(calls).toEqual([{ method: 'DELETE', path: '/api/wishlist' }]))
  })
})
