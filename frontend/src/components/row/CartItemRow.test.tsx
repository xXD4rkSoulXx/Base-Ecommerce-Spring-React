import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { CartItemRow } from '@/components/row/CartItemRow'
import { MAX_CART_ITEM_QUANTITY } from '@/constants'
import { buildGuestCartItem } from '@/test/builders'
import { priceText } from '@/test/format'
import { renderWithProviders } from '@/test/render'

const renderRow = (overrides: Parameters<typeof buildGuestCartItem>[0] = {}) => {
  const onQuantityChange = vi.fn()
  const onRemove = vi.fn()

  renderWithProviders(
    <CartItemRow
      item={buildGuestCartItem({ productId: 1, quantity: 2, stock: 5, ...overrides })}
      lineTotal={200}
      onQuantityChange={onQuantityChange}
      onRemove={onRemove}
    />
  )

  return { onQuantityChange, onRemove }
}

describe('CartItemRow', () => {
  it('shows the item data and the line total', () => {
    renderRow()

    expect(screen.getByText('REF-1')).toBeInTheDocument()
    expect(screen.getByText('Rain Jacket')).toBeInTheDocument()
    expect(screen.getByText('M')).toBeInTheDocument()
    expect(screen.getByText('BLACK')).toBeInTheDocument()
    expect(screen.getByText(priceText(200))).toBeInTheDocument()
    screen.getAllByRole('link').forEach((link) => expect(link).toHaveAttribute('href', '/product/1'))
  })

  it('asks to increase and to decrease the quantity by one', async () => {
    const user = userEvent.setup()
    const { onQuantityChange } = renderRow({ quantity: 2 })

    await user.click(screen.getByRole('button', { name: 'Increase quantity' }))
    await user.click(screen.getByRole('button', { name: 'Decrease quantity' }))

    expect(onQuantityChange).toHaveBeenNthCalledWith(1, 3)
    expect(onQuantityChange).toHaveBeenNthCalledWith(2, 1)
  })

  it('cannot go below one unit', () => {
    renderRow({ quantity: 1 })

    expect(screen.getByRole('button', { name: 'Decrease quantity' })).toBeDisabled()
  })

  it('cannot go above the available stock', () => {
    renderRow({ quantity: 3, stock: 3 })

    expect(screen.getByRole('button', { name: 'Increase quantity' })).toBeDisabled()
  })

  it('cannot go above the maximum per line even with plenty of stock', () => {
    renderRow({ quantity: MAX_CART_ITEM_QUANTITY, stock: 100 })

    expect(screen.getByRole('button', { name: 'Increase quantity' })).toBeDisabled()
  })

  it('removes the item', async () => {
    const user = userEvent.setup()
    const { onRemove } = renderRow()

    await user.click(screen.getByRole('button', { name: 'Remove Rain Jacket from the cart' }))

    expect(onRemove).toHaveBeenCalledTimes(1)
  })

  it('marks an unavailable item, hides the quantity controls and strikes the price', () => {
    renderRow({ available: false })

    expect(screen.getByText('Unavailable')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Increase quantity' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Decrease quantity' })).not.toBeInTheDocument()
    expect(screen.getByText(priceText(200))).toHaveClass('line-through')
  })
})
