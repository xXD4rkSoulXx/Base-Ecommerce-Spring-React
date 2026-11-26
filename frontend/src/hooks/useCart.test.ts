import { act, renderHook, waitFor } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useCart } from '@/hooks/useCart'
import { useAuthStore } from '@/store/useAuthStore'
import { useCartStore } from '@/store/useCartStore'
import { buildGuestCartItem, buildUser } from '@/test/builders'
import { createWrapper } from '@/test/render'
import { apiUrl, server } from '@/test/server'

const initialAuthState = useAuthStore.getInitialState()
const initialCartState = useCartStore.getInitialState()

interface Call {
  method: string
  path: string
  body: unknown
}

let calls: Call[]

const itemA = buildGuestCartItem({ productSkuId: 1, price: 100, discountPercentage: 20, quantity: 2 })
const itemB = buildGuestCartItem({ productSkuId: 2, price: 50, discountPercentage: 0, quantity: 1 })
const itemC = buildGuestCartItem({ productSkuId: 3, price: 999, discountPercentage: 0, quantity: 1, available: false })
const serverItem = { ...buildGuestCartItem({ productSkuId: 7, price: 100, quantity: 1 }), id: 1 }

const mockCartApi = (cart: object = { items: [serverItem], discountPercentage: 0 }) =>
  server.use(
    http.all(`${apiUrl('/api/cart')}*`, async ({ request }) => {
      if (request.method !== 'GET') {
        calls.push({
          method: request.method,
          path: new URL(request.url).pathname,
          body: await request
            .clone()
            .json()
            .catch(() => undefined)
        })
      }

      return HttpResponse.json(cart)
    })
  )

const signIn = () => useAuthStore.getState().setAuth(buildUser(), 'token')

const renderCart = () => renderHook(() => useCart(), { wrapper: createWrapper() })

describe('useCart as a guest', () => {
  beforeEach(() => {
    useAuthStore.setState(initialAuthState, true)
    useCartStore.setState(initialCartState, true)
    localStorage.clear()
    calls = []
  })

  it('reads the guest cart and counts all the units', () => {
    useCartStore.setState({ items: [itemA, itemB, itemC] })

    const { result } = renderCart()

    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.items).toHaveLength(3)
    expect(result.current.totalItems).toBe(4)
  })

  it('adds up the discounted line totals and ignores unavailable items in the subtotal', () => {
    useCartStore.setState({ items: [itemA, itemB, itemC] })

    const { result } = renderCart()

    expect(result.current.getLineTotal(itemA)).toBe(160)
    expect(result.current.subtotal).toBe(210)
  })

  it('has no discount code', () => {
    useCartStore.setState({ items: [itemA] })

    const { result } = renderCart()

    expect(result.current.discountCode).toBeNull()
    expect(result.current.discountPercentage).toBe(0)
    expect(result.current.discountAmount).toBe(0)
  })

  it('knows which variants are in the cart and in what quantity', () => {
    useCartStore.setState({ items: [itemA] })

    const { result } = renderCart()

    expect(result.current.isInCart(1)).toBe(true)
    expect(result.current.isInCart(2)).toBe(false)
    expect(result.current.getQuantity(1)).toBe(2)
    expect(result.current.getQuantity(2)).toBe(0)
  })

  it('adds an item to the local cart and calls onSuccess', () => {
    const onSuccess = vi.fn()
    const { result } = renderCart()

    act(() => result.current.addToCart(itemB, onSuccess))

    expect(useCartStore.getState().items).toEqual([itemB])
    expect(onSuccess).toHaveBeenCalledTimes(1)
  })

  it('updates, removes and clears items in the local cart', () => {
    useCartStore.setState({ items: [itemA, itemB] })
    const { result } = renderCart()

    act(() => result.current.updateQuantity(1, 5))
    expect(useCartStore.getState().items.find((item) => item.productSkuId === 1)?.quantity).toBe(5)

    act(() => result.current.removeFromCart(2))
    expect(useCartStore.getState().items.map((item) => item.productSkuId)).toEqual([1])

    act(() => result.current.clearCart())
    expect(useCartStore.getState().items).toEqual([])
  })
})

describe('useCart when signed in', () => {
  beforeEach(() => {
    useAuthStore.setState(initialAuthState, true)
    useCartStore.setState(initialCartState, true)
    localStorage.clear()
    calls = []
    signIn()
  })

  it('loads the cart and the discount from the server', async () => {
    mockCartApi({ items: [serverItem], discountCode: 'SAVE10', discountPercentage: 10 })

    const { result } = renderCart()

    await waitFor(() => expect(result.current.items).toHaveLength(1))

    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.discountCode).toBe('SAVE10')
    expect(result.current.discountPercentage).toBe(10)
    expect(result.current.subtotal).toBe(100)
    expect(result.current.discountAmount).toBe(10)
  })

  it('ignores the guest cart', async () => {
    useCartStore.setState({ items: [itemA] })
    mockCartApi()

    const { result } = renderCart()

    await waitFor(() => expect(result.current.items).toHaveLength(1))

    expect(result.current.items[0].productSkuId).toBe(7)
  })

  it('adds an item on the server, calls onSuccess and leaves the local cart alone', async () => {
    mockCartApi()
    const onSuccess = vi.fn()
    const { result } = renderCart()

    act(() => result.current.addToCart(itemB, onSuccess))

    await waitFor(() => expect(onSuccess).toHaveBeenCalledTimes(1))

    expect(calls).toEqual([{ method: 'POST', path: '/api/cart', body: { productSkuId: 2, quantity: 1 } }])
    expect(useCartStore.getState().items).toEqual([])
  })

  it('updates the quantity on the server', async () => {
    mockCartApi()
    const { result } = renderCart()

    act(() => result.current.updateQuantity(7, 3))

    await waitFor(() => expect(calls).toEqual([{ method: 'PUT', path: '/api/cart/7', body: { quantity: 3 } }]))
  })

  it('removes an item on the server', async () => {
    mockCartApi()
    const { result } = renderCart()

    act(() => result.current.removeFromCart(7))

    await waitFor(() => expect(calls).toEqual([{ method: 'DELETE', path: '/api/cart/7', body: undefined }]))
  })

  it('clears the cart on the server', async () => {
    mockCartApi()
    const { result } = renderCart()

    act(() => result.current.clearCart())

    await waitFor(() => expect(calls).toEqual([{ method: 'DELETE', path: '/api/cart', body: undefined }]))
  })

  it('applies and removes a discount code on the server', async () => {
    mockCartApi()
    const { result } = renderCart()

    act(() => result.current.applyDiscountCode('SAVE10'))
    await waitFor(() => expect(calls).toHaveLength(1))

    act(() => result.current.removeDiscountCode())
    await waitFor(() => expect(calls).toHaveLength(2))

    expect(calls).toEqual([
      { method: 'POST', path: '/api/cart/discount', body: { code: 'SAVE10' } },
      { method: 'DELETE', path: '/api/cart/discount', body: undefined }
    ])
  })

  it('exposes the error of an invalid discount code', async () => {
    mockCartApi()
    server.use(http.post(apiUrl('/api/cart/discount'), () => new HttpResponse(null, { status: 400 })))
    const { result } = renderCart()

    act(() => result.current.applyDiscountCode('WRONG'))

    await waitFor(() => expect(result.current.isDiscountCodeError).toBe(true))
  })
})
