import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ProductGallerySection } from '@/components/section/ProductGallerySection'
import { useAuthStore } from '@/store/useAuthStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { buildProduct, buildProductDetails } from '@/test/builders'
import { renderWithProviders } from '@/test/render'

const initialAuthState = useAuthStore.getInitialState()
const initialWishlistState = useWishlistStore.getInitialState()

const images = ['/img/a.webp', '/img/b.webp', '/img/c.webp']

const renderGallery = (galleryImages = images, activeIndex = 0) => {
  const onSelect = vi.fn()

  renderWithProviders(
    <ProductGallerySection
      product={buildProductDetails({ discountPercentage: 20 })}
      images={galleryImages}
      activeIndex={activeIndex}
      onSelect={onSelect}
    />
  )

  return { onSelect }
}

describe('ProductGallerySection', () => {
  beforeEach(() => {
    useAuthStore.setState(initialAuthState, true)
    useWishlistStore.setState(initialWishlistState, true)
    localStorage.clear()
  })

  it('shows the active image and the product reference', () => {
    renderGallery(images, 1)

    expect(screen.getByRole('img', { name: 'Rain Jacket' })).toHaveAttribute('src', '/img/b.webp')
    expect(screen.getByText('REF: REF-1')).toBeInTheDocument()
  })

  it('shows one thumbnail per image and marks the active one', () => {
    renderGallery(images, 1)

    expect(screen.getAllByRole('button', { name: /^View image/ })).toHaveLength(3)
    expect(screen.getByRole('button', { name: 'View image 2' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: 'View image 1' })).toHaveAttribute('aria-pressed', 'false')
  })

  it('does not show thumbnails when there is a single image', () => {
    renderGallery(['/img/a.webp'])

    expect(screen.queryByRole('button', { name: /^View image/ })).not.toBeInTheDocument()
  })

  it('reports the index of the thumbnail that was clicked', async () => {
    const user = userEvent.setup()
    const { onSelect } = renderGallery()

    await user.click(screen.getByRole('button', { name: 'View image 3' }))

    expect(onSelect).toHaveBeenCalledWith(2)
  })

  it('adds the product to the wishlist using the first image as the cover, whatever image is active', async () => {
    const user = userEvent.setup()

    renderGallery(images, 2)
    await user.click(screen.getByRole('button', { name: 'Add to wishlist' }))

    expect(useWishlistStore.getState().items).toEqual([
      {
        id: 1,
        ref: 'REF-1',
        name: 'Rain Jacket',
        price: 100,
        discountPercentage: 20,
        category: 'OUTERWEAR',
        cover: '/img/a.webp'
      }
    ])
    expect(screen.getByRole('button', { name: 'Remove from wishlist' })).toHaveAttribute('aria-pressed', 'true')
  })

  it('removes the product from the wishlist when it is already a favorite', async () => {
    const user = userEvent.setup()

    useWishlistStore.getState().addToWishlist(buildProduct({ id: 1 }))
    renderGallery()
    await user.click(screen.getByRole('button', { name: 'Remove from wishlist' }))

    expect(useWishlistStore.getState().items).toEqual([])
  })
})
