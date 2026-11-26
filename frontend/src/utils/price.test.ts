import { describe, expect, it } from 'vitest'
import { getDiscountedPrice } from '@/utils/price'

describe('getDiscountedPrice', () => {
  it.each([
    [100, 0, 100],
    [100, 25, 75],
    [19.99, 10, 17.99],
    [33.33, 33, 22.33],
    [59.9, 15, 50.92],
    [100, 100, 0],
    [0, 50, 0]
  ])('%s with %s%% off is %s', (price, discountPercentage, expected) => {
    expect(getDiscountedPrice(price, discountPercentage)).toBe(expected)
  })

  it('rounds half a cent up', () => {
    expect(getDiscountedPrice(9.99, 50)).toBe(5)
  })

  it('does not lose a cent to floating point errors', () => {
    expect(getDiscountedPrice(1, 99)).toBe(0.01)
  })
})
