import { beforeEach, describe, expect, it } from 'vitest'
import { MAX_CART_ITEM_QUANTITY, MAX_CART_LINES } from '@/constants'
import { useCartStore } from '@/store/useCartStore'
import { buildGuestCartItem } from '@/test/builders'

const STORAGE_KEY = 'velociti-cart'

const initialState = useCartStore.getInitialState()

const cart = () => useCartStore.getState()

const addLines = (count: number) => {
  for (let productSkuId = 1; productSkuId <= count; productSkuId += 1) {
    cart().addToCart(buildGuestCartItem({ productSkuId }))
  }
}

beforeEach(() => {
  localStorage.clear()
  useCartStore.setState(initialState, true)
})

describe('useCartStore', () => {
  describe('addToCart', () => {
    it('adds a new line', () => {
      cart().addToCart(buildGuestCartItem({ productSkuId: 7, quantity: 2 }))

      expect(cart().items).toHaveLength(1)
      expect(cart().items[0]).toMatchObject({ productSkuId: 7, quantity: 2 })
    })

    it('adds the quantity to the existing line of the same sku', () => {
      cart().addToCart(buildGuestCartItem({ productSkuId: 7, quantity: 2 }))
      cart().addToCart(buildGuestCartItem({ productSkuId: 7, quantity: 3 }))

      expect(cart().items).toHaveLength(1)
      expect(cart().items[0].quantity).toBe(5)
    })

    it('caps the summed quantity of an existing line', () => {
      cart().addToCart(buildGuestCartItem({ quantity: MAX_CART_ITEM_QUANTITY - 2 }))
      cart().addToCart(buildGuestCartItem({ quantity: 5 }))

      expect(cart().items[0].quantity).toBe(MAX_CART_ITEM_QUANTITY)
    })

    it('caps the quantity of a new line', () => {
      cart().addToCart(buildGuestCartItem({ quantity: MAX_CART_ITEM_QUANTITY + 5 }))

      expect(cart().items[0].quantity).toBe(MAX_CART_ITEM_QUANTITY)
    })

    it('ignores a new line when the cart is full', () => {
      addLines(MAX_CART_LINES)

      cart().addToCart(buildGuestCartItem({ productSkuId: 999 }))

      expect(cart().items).toHaveLength(MAX_CART_LINES)
      expect(cart().items.some((item) => item.productSkuId === 999)).toBe(false)
    })

    it('still increases an existing line when the cart is full', () => {
      addLines(MAX_CART_LINES)

      cart().addToCart(buildGuestCartItem({ productSkuId: 1, quantity: 2 }))

      expect(cart().items).toHaveLength(MAX_CART_LINES)
      expect(cart().items.find((item) => item.productSkuId === 1)?.quantity).toBe(3)
    })
  })

  describe('getTotalItems', () => {
    it('is zero for an empty cart', () => {
      expect(cart().getTotalItems()).toBe(0)
    })

    it('sums the quantities of all lines', () => {
      cart().addToCart(buildGuestCartItem({ productSkuId: 1, quantity: 2 }))
      cart().addToCart(buildGuestCartItem({ productSkuId: 2, quantity: 3 }))

      expect(cart().getTotalItems()).toBe(5)
    })
  })

  describe('updateQuantity', () => {
    it('sets the quantity of the line', () => {
      cart().addToCart(buildGuestCartItem({ productSkuId: 7, quantity: 1 }))

      cart().updateQuantity(7, 4)

      expect(cart().items[0].quantity).toBe(4)
    })

    it('caps the quantity', () => {
      cart().addToCart(buildGuestCartItem({ productSkuId: 7, quantity: 1 }))

      cart().updateQuantity(7, MAX_CART_ITEM_QUANTITY + 5)

      expect(cart().items[0].quantity).toBe(MAX_CART_ITEM_QUANTITY)
    })

    it('removes the line when the quantity is zero or less', () => {
      cart().addToCart(buildGuestCartItem({ productSkuId: 7 }))
      cart().addToCart(buildGuestCartItem({ productSkuId: 8 }))

      cart().updateQuantity(7, 0)

      expect(cart().items.map((item) => item.productSkuId)).toEqual([8])
    })

    it('does not change anything for an unknown sku', () => {
      cart().addToCart(buildGuestCartItem({ productSkuId: 7, quantity: 2 }))
      const before = cart().items

      cart().updateQuantity(999, 5)

      expect(cart().items).toEqual(before)
    })
  })

  describe('removeFromCart and clearCart', () => {
    it('removeFromCart removes only that line', () => {
      cart().addToCart(buildGuestCartItem({ productSkuId: 7 }))
      cart().addToCart(buildGuestCartItem({ productSkuId: 8 }))

      cart().removeFromCart(7)

      expect(cart().items.map((item) => item.productSkuId)).toEqual([8])
    })

    it('clearCart removes every line', () => {
      addLines(3)

      cart().clearCart()

      expect(cart().items).toEqual([])
    })
  })

  describe('persistence', () => {
    it('saves the items in localStorage', () => {
      cart().addToCart(buildGuestCartItem({ productSkuId: 7 }))

      const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '{}')

      expect(stored.state.items).toHaveLength(1)
      expect(stored.state.items[0].productSkuId).toBe(7)
    })

    it('restores the items from localStorage', async () => {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({ state: { items: [buildGuestCartItem({ productSkuId: 7 })] }, version: 0 })
      )

      await useCartStore.persist.rehydrate()

      expect(cart().items.map((item) => item.productSkuId)).toEqual([7])
    })
  })
})
