import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { toast } from 'sonner'
import { ProductOverviewSection } from '@/components/section/ProductOverviewSection'
import { useAuthStore } from '@/store/useAuthStore'
import { useCartStore } from '@/store/useCartStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { buildProductDetails, buildProductSku } from '@/test/builders'
import { priceText } from '@/test/format'
import { renderWithProviders } from '@/test/render'

vi.mock('sonner', () => ({ toast: { success: vi.fn() } }))

const initialAuthState = useAuthStore.getInitialState()
const initialCartState = useCartStore.getInitialState()
const initialWishlistState = useWishlistStore.getInitialState()

const skus = [
  buildProductSku({ id: 1, size: 'S', color: 'BLACK', stock: 3 }),
  buildProductSku({ id: 2, size: 'M', color: 'BLACK', stock: 0 }),
  buildProductSku({ id: 3, size: 'S', color: 'WHITE', stock: 4, paths: ['/img/white-1.webp'] }),
  buildProductSku({ id: 4, size: 'S', color: 'RED', stock: 0, paths: [] })
]

const renderOverview = (overrides: Parameters<typeof buildProductDetails>[0] = {}) =>
  renderWithProviders(<ProductOverviewSection product={buildProductDetails({ productSkus: skus, ...overrides })} />)

const addToCartButton = () => screen.getByRole('button', { name: /^(Select a Size|Add to Cart|Out of Stock)$/ })

describe('ProductOverviewSection', () => {
  beforeEach(() => {
    useAuthStore.setState(initialAuthState, true)
    useCartStore.setState(initialCartState, true)
    useWishlistStore.setState(initialWishlistState, true)
    localStorage.clear()
  })

  it('shows the product data', () => {
    renderOverview()

    expect(screen.getByRole('heading', { name: 'Rain Jacket' })).toBeInTheDocument()
    expect(screen.getByText('OUTERWEAR')).toBeInTheDocument()
    expect(screen.getByText('4.5')).toBeInTheDocument()
    expect(screen.getByText('/ 3 reviews')).toBeInTheDocument()
    expect(screen.getByText('Waterproof jacket.')).toBeInTheDocument()
    expect(screen.getByText(priceText(100))).toBeInTheDocument()
  })

  it('uses the singular for a single review', () => {
    renderOverview({ totalReviews: 1 })

    expect(screen.getByText('/ 1 review')).toBeInTheDocument()
  })

  it('starts with the first color selected and only its sizes', () => {
    renderOverview()

    expect(screen.getByRole('button', { name: 'BLACK' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getAllByRole('button', { name: /^(S|M)$/ })).toHaveLength(2)
  })

  it('disables the sizes and the colors without stock', () => {
    renderOverview()

    expect(screen.getByRole('button', { name: 'M' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'S' })).toBeEnabled()
    expect(screen.getByRole('button', { name: 'RED' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'WHITE' })).toBeEnabled()
  })

  it('asks for a size and shows the total stock before one is selected', () => {
    renderOverview()

    expect(addToCartButton()).toHaveTextContent('Select a Size')
    expect(addToCartButton()).toBeDisabled()
    expect(screen.getByText('Stock: 7 units total')).toBeInTheDocument()
  })

  it('enables add to cart and shows the stock of the selected size', async () => {
    const user = userEvent.setup()

    renderOverview()
    await user.click(screen.getByRole('button', { name: 'S' }))

    expect(addToCartButton()).toHaveTextContent('Add to Cart')
    expect(addToCartButton()).toBeEnabled()
    expect(screen.getByText('Stock: 3 units')).toBeInTheDocument()
  })

  it('clears the selected size when the color changes', async () => {
    const user = userEvent.setup()

    renderOverview()
    await user.click(screen.getByRole('button', { name: 'S' }))
    await user.click(screen.getByRole('button', { name: 'WHITE' }))

    expect(screen.getByRole('button', { name: 'WHITE' })).toHaveAttribute('aria-pressed', 'true')
    expect(addToCartButton()).toHaveTextContent('Select a Size')
  })

  it('shows the images of the selected color', async () => {
    const user = userEvent.setup()

    renderOverview()

    expect(screen.getByRole('img', { name: 'Rain Jacket' })).toHaveAttribute('src', '/img/black-1.webp')

    await user.click(screen.getByRole('button', { name: 'WHITE' }))

    expect(screen.getByRole('img', { name: 'Rain Jacket' })).toHaveAttribute('src', '/img/white-1.webp')
  })

  it('falls back to the first image of any variant when the color has none', () => {
    renderOverview({
      productSkus: [
        buildProductSku({ id: 1, color: 'BLACK', paths: [] }),
        buildProductSku({ id: 2, color: 'WHITE', paths: ['/img/white-1.webp'] })
      ]
    })

    expect(screen.getByRole('img', { name: 'Rain Jacket' })).toHaveAttribute('src', '/img/white-1.webp')
  })

  it('shows the product discount', () => {
    renderOverview({ discountPercentage: 20 })

    expect(screen.getByText('-20% off')).toBeInTheDocument()
    expect(screen.getByText(priceText(80))).toBeInTheDocument()
    expect(screen.getByText(priceText(100))).toHaveClass('line-through')
  })

  it('does not show a discount without discount', () => {
    renderOverview()

    expect(screen.queryByText(/% off/)).not.toBeInTheDocument()
  })

  it('uses the discount of the selected variant over the product discount', async () => {
    const user = userEvent.setup()

    renderOverview({
      discountPercentage: 20,
      productSkus: [
        buildProductSku({ id: 1, size: 'S', discountPercentage: 50 }),
        buildProductSku({ id: 2, size: 'M' })
      ]
    })

    await user.click(screen.getByRole('button', { name: 'S' }))

    expect(screen.getByText('-50% off')).toBeInTheDocument()
    expect(screen.getByText(priceText(50))).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'M' }))

    expect(screen.getByText('-20% off')).toBeInTheDocument()
  })

  it('adds the selected variant to the guest cart and confirms with a toast', async () => {
    const user = userEvent.setup()

    renderOverview({ discountPercentage: 20 })
    await user.click(screen.getByRole('button', { name: 'S' }))
    await user.click(screen.getByRole('button', { name: 'Add to Cart' }))

    expect(useCartStore.getState().items).toEqual([
      expect.objectContaining({
        productSkuId: 1,
        productId: 1,
        productName: 'Rain Jacket',
        productRef: 'REF-1',
        size: 'S',
        color: 'BLACK',
        cover: '/img/black-1.webp',
        price: 100,
        discountPercentage: 20,
        stock: 3,
        quantity: 1
      })
    ])
    expect(toast.success).toHaveBeenCalledWith('Rain Jacket added to cart')
  })

  describe('without variants', () => {
    it('renders without crashing and cannot add to cart', () => {
      renderOverview({ productSkus: [] })

      expect(screen.getByRole('heading', { name: 'Rain Jacket' })).toBeInTheDocument()
      expect(screen.getByText('Stock: 0 units total')).toBeInTheDocument()
      expect(screen.getByText('No variants for this color')).toBeInTheDocument()
      expect(addToCartButton()).toHaveTextContent('Select a Size')
      expect(addToCartButton()).toBeDisabled()
    })
  })
})
