import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ProductPrice } from '@/components/ui/ProductPrice'
import { priceText } from '@/test/format'

describe('ProductPrice', () => {
  it('shows only the price when there is no discount', () => {
    render(<ProductPrice price={100} discountPercentage={0} />)

    expect(screen.getByText(priceText(100))).toBeInTheDocument()
  })

  it('shows the discounted price and the original price struck through', () => {
    render(<ProductPrice price={100} discountPercentage={20} />)

    expect(screen.getByText(priceText(80))).toBeInTheDocument()
    expect(screen.getByText(priceText(100))).toHaveClass('line-through')
  })

  it('rounds the discounted price to cents', () => {
    render(<ProductPrice price={19.99} discountPercentage={15} />)

    expect(screen.getByText(priceText(16.99))).toBeInTheDocument()
  })
})
