import { beforeEach, describe, expect, it } from 'vitest'
import { MAX_GUEST_WISHLIST_ITEMS } from '@/constants'
import { useWishlistStore } from '@/store/useWishlistStore'
import { buildProduct } from '@/test/builders'

const initialState = useWishlistStore.getInitialState()

const wishlist = () => useWishlistStore.getState()

beforeEach(() => {
  localStorage.clear()
  useWishlistStore.setState(initialState, true)
})

describe('useWishlistStore', () => {
  it('adds a product', () => {
    wishlist().addToWishlist(buildProduct({ id: 7 }))

    expect(wishlist().items.map((item) => item.id)).toEqual([7])
  })

  it('does not add the same product twice', () => {
    wishlist().addToWishlist(buildProduct({ id: 7 }))
    wishlist().addToWishlist(buildProduct({ id: 7 }))

    expect(wishlist().items).toHaveLength(1)
  })

  it('ignores new products when the wishlist is full', () => {
    for (let id = 1; id <= MAX_GUEST_WISHLIST_ITEMS; id += 1) {
      wishlist().addToWishlist(buildProduct({ id }))
    }

    wishlist().addToWishlist(buildProduct({ id: 9999 }))

    expect(wishlist().items).toHaveLength(MAX_GUEST_WISHLIST_ITEMS)
    expect(wishlist().items.some((item) => item.id === 9999)).toBe(false)
  })

  it('counts the products', () => {
    wishlist().addToWishlist(buildProduct({ id: 1 }))
    wishlist().addToWishlist(buildProduct({ id: 2 }))

    expect(wishlist().getTotalItems()).toBe(2)
  })

  it('removes only the given product', () => {
    wishlist().addToWishlist(buildProduct({ id: 1 }))
    wishlist().addToWishlist(buildProduct({ id: 2 }))

    wishlist().removeFromWishlist(1)

    expect(wishlist().items.map((item) => item.id)).toEqual([2])
  })

  it('clears every product', () => {
    wishlist().addToWishlist(buildProduct({ id: 1 }))

    wishlist().clearWishlist()

    expect(wishlist().items).toEqual([])
  })

  it('saves the items in localStorage', () => {
    wishlist().addToWishlist(buildProduct({ id: 7 }))

    const stored = JSON.parse(localStorage.getItem('velociti-wishlist') ?? '{}')

    expect(stored.state.items[0].id).toBe(7)
  })
})
