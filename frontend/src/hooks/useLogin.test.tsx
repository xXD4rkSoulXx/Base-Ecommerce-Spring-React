import { QueryClientProvider } from '@tanstack/react-query'
import { act, renderHook, screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import type { ReactNode } from 'react'
import { MemoryRouter, type MemoryRouterProps, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { toast } from 'sonner'
import { useLogin } from '@/hooks/useLogin'
import { queryKeys } from '@/queryKeys'
import { useAuthStore } from '@/store/useAuthStore'
import { useCartStore } from '@/store/useCartStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { buildGuestCartItem, buildProduct, buildUser } from '@/test/builders'
import { createTestQueryClient } from '@/test/render'
import { apiUrl, server } from '@/test/server'

vi.mock('sonner', () => ({ toast: { error: vi.fn() } }))

const initialAuthState = useAuthStore.getInitialState()
const initialCartState = useCartStore.getInitialState()
const initialWishlistState = useWishlistStore.getInitialState()

const user = buildUser()
const credentials = { email: 'ana@mail.com', password: 'Abcdefg1' }
const WISHLIST_ERROR = 'Could not sync your wishlist. It will be retried on your next login.'
const CART_ERROR = 'Could not sync your cart. It will be retried on your next login.'

let wishlistMerge: unknown
let cartMerge: unknown

const LocationProbe = () => <p data-testid="location">{useLocation().pathname}</p>

const mockLogin = () =>
  server.use(http.post(apiUrl('/api/auth/login'), () => HttpResponse.json({ user, accessToken: 'new-token' })))

const mockWishlistMerge = (status = 204) =>
  server.use(
    http.post(apiUrl('/api/wishlist/merge'), async ({ request }) => {
      wishlistMerge = await request.json()

      return new HttpResponse(null, { status })
    })
  )

const mockCartMerge = (status = 204) =>
  server.use(
    http.post(apiUrl('/api/cart/merge'), async ({ request }) => {
      cartMerge = await request.json()

      return new HttpResponse(null, { status })
    })
  )

const renderLogin = (initialEntries: MemoryRouterProps['initialEntries'] = ['/login']) => {
  const queryClient = createTestQueryClient()
  const invalidateQueries = vi.spyOn(queryClient, 'invalidateQueries')

  const wrapper = ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={initialEntries}>
        {children}
        <LocationProbe />
      </MemoryRouter>
    </QueryClientProvider>
  )

  return { ...renderHook(() => useLogin(), { wrapper }), invalidateQueries }
}

const login = (result: ReturnType<typeof renderLogin>['result']) => act(() => result.current.mutateAsync(credentials))

describe('useLogin', () => {
  beforeEach(() => {
    useAuthStore.setState(initialAuthState, true)
    useCartStore.setState(initialCartState, true)
    useWishlistStore.setState(initialWishlistState, true)
    localStorage.clear()
    wishlistMerge = undefined
    cartMerge = undefined
  })

  it('stores the session and goes to the home page', async () => {
    mockLogin()
    const { result } = renderLogin()

    await login(result)

    expect(useAuthStore.getState().user).toEqual(user)
    expect(useAuthStore.getState().accessToken).toBe('new-token')
    expect(screen.getByTestId('location')).toHaveTextContent('/')
  })

  it('goes back to the page the user came from', async () => {
    mockLogin()
    const { result } = renderLogin([{ pathname: '/login', state: { from: { pathname: '/cart' } } }])

    await login(result)

    expect(screen.getByTestId('location')).toHaveTextContent('/cart')
  })

  it('refreshes the wishlist and the cart from the server', async () => {
    mockLogin()
    const { result, invalidateQueries } = renderLogin()

    await login(result)

    expect(invalidateQueries).toHaveBeenCalledWith({ queryKey: queryKeys.wishlist })
    expect(invalidateQueries).toHaveBeenCalledWith({ queryKey: queryKeys.cart })
  })

  it('merges the guest wishlist into the account and clears it', async () => {
    mockLogin()
    mockWishlistMerge()
    useWishlistStore.getState().addToWishlist(buildProduct({ id: 1 }))
    useWishlistStore.getState().addToWishlist(buildProduct({ id: 2 }))
    const { result } = renderLogin()

    await login(result)

    expect(wishlistMerge).toEqual({ productIds: [1, 2] })
    expect(useWishlistStore.getState().items).toEqual([])
  })

  it('merges the guest cart into the account and clears it', async () => {
    mockLogin()
    mockCartMerge()
    useCartStore.getState().addToCart(buildGuestCartItem({ productSkuId: 7, quantity: 2 }))
    const { result } = renderLogin()

    await login(result)

    expect(cartMerge).toEqual({ items: [{ productSkuId: 7, quantity: 2 }] })
    expect(useCartStore.getState().items).toEqual([])
  })

  it('keeps the guest wishlist, warns the user and still merges the cart when the wishlist merge fails', async () => {
    mockLogin()
    mockWishlistMerge(500)
    mockCartMerge()
    useWishlistStore.getState().addToWishlist(buildProduct({ id: 1 }))
    useCartStore.getState().addToCart(buildGuestCartItem({ productSkuId: 7 }))
    const { result } = renderLogin()

    await login(result)

    expect(useWishlistStore.getState().items).toHaveLength(1)
    expect(toast.error).toHaveBeenCalledWith(WISHLIST_ERROR)
    expect(useCartStore.getState().items).toEqual([])
    expect(screen.getByTestId('location')).toHaveTextContent('/')
  })

  it('keeps the guest cart, warns the user and still logs in when the cart merge fails', async () => {
    mockLogin()
    mockCartMerge(500)
    useCartStore.getState().addToCart(buildGuestCartItem({ productSkuId: 7 }))
    const { result } = renderLogin()

    await login(result)

    expect(useCartStore.getState().items).toHaveLength(1)
    expect(toast.error).toHaveBeenCalledWith(CART_ERROR)
    expect(useAuthStore.getState().accessToken).toBe('new-token')
    expect(screen.getByTestId('location')).toHaveTextContent('/')
  })

  it('does not call the merge endpoints when there is nothing from the guest session', async () => {
    mockLogin()
    const { result } = renderLogin()

    await login(result)

    expect(wishlistMerge).toBeUndefined()
    expect(cartMerge).toBeUndefined()
  })

  it('keeps everything untouched when the credentials are wrong', async () => {
    server.use(http.post(apiUrl('/api/auth/login'), () => new HttpResponse(null, { status: 401 })))
    useWishlistStore.getState().addToWishlist(buildProduct())
    const { result } = renderLogin()

    await expect(login(result)).rejects.toMatchObject({ response: { status: 401 } })

    expect(useAuthStore.getState().accessToken).toBeNull()
    expect(useWishlistStore.getState().items).toHaveLength(1)
    expect(screen.getByTestId('location')).toHaveTextContent('/login')
  })
})
