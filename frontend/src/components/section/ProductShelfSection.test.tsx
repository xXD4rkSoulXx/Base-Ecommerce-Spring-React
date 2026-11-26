import { screen } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { ProductShelfSection } from '@/components/section/ProductShelfSection'
import { useAuthStore } from '@/store/useAuthStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { buildProduct } from '@/test/builders'
import { renderWithProviders } from '@/test/render'
import type { ProductResponse } from '@/types/product'

const initialAuthState = useAuthStore.getInitialState()
const initialWishlistState = useWishlistStore.getInitialState()

interface RenderOptions {
  products?: ProductResponse[]
  isLoading?: boolean
}

const renderShelf = ({ products, isLoading = false }: RenderOptions = {}) =>
  renderWithProviders(
    <ProductShelfSection
      title="New "
      highlight="Arrivals"
      accent="pink"
      linkTo="/shop?sortBy=recent"
      products={products}
      isLoading={isLoading}
    />
  )

describe('ProductShelfSection', () => {
  beforeEach(() => {
    useAuthStore.setState(initialAuthState, true)
    useWishlistStore.setState(initialWishlistState, true)
    localStorage.clear()
  })

  it('shows the title, the link to the full listing and one card per product', () => {
    renderShelf({
      products: [buildProduct({ id: 1, name: 'Rain Jacket' }), buildProduct({ id: 2, name: 'Wind Shell' })]
    })

    expect(screen.getByRole('heading', { name: 'New Arrivals' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /View All/ })).toHaveAttribute('href', '/shop?sortBy=recent')
    expect(screen.getByRole('heading', { name: 'Rain Jacket' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Wind Shell' })).toBeInTheDocument()
  })

  it('highlights the second part of the title with the accent color', () => {
    renderShelf({ products: [buildProduct()] })

    expect(screen.getByText('Arrivals')).toHaveClass('text-neon-pink')
  })

  it('shows placeholders instead of products while loading', () => {
    const { container } = renderShelf({ isLoading: true, products: [buildProduct()] })

    expect(container.querySelectorAll('.animate-pulse')).toHaveLength(8)
    expect(screen.queryByRole('heading', { name: 'Rain Jacket' })).not.toBeInTheDocument()
  })

  it.each([
    ['undefined', undefined],
    ['an empty list', []]
  ])('renders nothing when there are no products (%s)', (_, products) => {
    const { container } = renderShelf({ products })

    expect(container).toBeEmptyDOMElement()
  })
})
