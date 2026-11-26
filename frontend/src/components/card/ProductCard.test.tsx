import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import { ProductCard } from '@/components/card/ProductCard'
import { useAuthStore } from '@/store/useAuthStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { buildProduct } from '@/test/builders'
import { priceText } from '@/test/format'
import { renderWithProviders } from '@/test/render'

const initialAuthState = useAuthStore.getInitialState()
const initialWishlistState = useWishlistStore.getInitialState()

describe('ProductCard', () => {
  beforeEach(() => {
    useAuthStore.setState(initialAuthState, true)
    useWishlistStore.setState(initialWishlistState, true)
    localStorage.clear()
  })

  it('shows the product data and links to its page', () => {
    renderWithProviders(<ProductCard product={buildProduct()} index={0} />)

    expect(screen.getByRole('heading', { name: 'Rain Jacket' })).toBeInTheDocument()
    expect(screen.getByText('REF: REF-1')).toBeInTheDocument()
    expect(screen.getByText('OUTERWEAR')).toBeInTheDocument()
    expect(screen.getByText(priceText(100))).toBeInTheDocument()
    expect(screen.getByRole('link')).toHaveAttribute('href', '/product/1')
  })

  it('does not show a discount badge without discount', () => {
    renderWithProviders(<ProductCard product={buildProduct()} index={0} />)

    expect(screen.queryByText(/^-\d+%$/)).not.toBeInTheDocument()
  })

  it('shows the discount badge and the discounted price', () => {
    renderWithProviders(<ProductCard product={buildProduct({ discountPercentage: 20 })} index={0} />)

    expect(screen.getByText('-20%')).toBeInTheDocument()
    expect(screen.getByText(priceText(80))).toBeInTheDocument()
  })

  it('adds the product to the wishlist and removes it on the second click', async () => {
    const user = userEvent.setup()

    renderWithProviders(<ProductCard product={buildProduct()} index={0} />)

    await user.click(screen.getByRole('button', { name: 'Add to wishlist' }))

    expect(screen.getByRole('button', { name: 'Remove from wishlist' })).toHaveAttribute('aria-pressed', 'true')
    expect(useWishlistStore.getState().items.map((item) => item.id)).toEqual([1])

    await user.click(screen.getByRole('button', { name: 'Remove from wishlist' }))

    expect(screen.getByRole('button', { name: 'Add to wishlist' })).toHaveAttribute('aria-pressed', 'false')
    expect(useWishlistStore.getState().items).toEqual([])
  })

  it('starts marked as favorite when the product is already in the wishlist', () => {
    useWishlistStore.getState().addToWishlist(buildProduct())

    renderWithProviders(<ProductCard product={buildProduct()} index={0} />)

    expect(screen.getByRole('button', { name: 'Remove from wishlist' })).toBeInTheDocument()
  })
})
