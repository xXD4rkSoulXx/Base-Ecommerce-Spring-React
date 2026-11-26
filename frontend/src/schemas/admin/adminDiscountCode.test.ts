import { describe, expect, it } from 'vitest'
import { discountCodeSchema } from '@/schemas/admin/adminDiscountCode'
import { issuesOf } from '@/test/schema'

describe('discountCodeSchema', () => {
  it('accepts a valid discount code', () => {
    expect(issuesOf(discountCodeSchema, { code: 'SAVE10', discountPercentage: 10 })).toEqual([])
  })

  it('trims and uppercases the code', () => {
    expect(discountCodeSchema.parse({ code: '  save-10_x ', discountPercentage: 10 }).code).toBe('SAVE-10_X')
  })

  it.each(['abc', 'a'.repeat(20)])('accepts the code %j at the length limits', (code) => {
    expect(issuesOf(discountCodeSchema, { code, discountPercentage: 10 })).toEqual([])
  })

  it.each(['ab', 'a'.repeat(21), 'save 10', 'save%', ''])('rejects the code %j', (code) => {
    expect(issuesOf(discountCodeSchema, { code, discountPercentage: 10 })).toEqual([
      { path: 'code', message: 'Code must have 3 to 20 characters: letters, numbers, - or _' }
    ])
  })

  it.each([1, 50, 100])('accepts a discount of %s', (discountPercentage) => {
    expect(issuesOf(discountCodeSchema, { code: 'SAVE10', discountPercentage })).toEqual([])
  })

  it.each([0, 101, 10.5, NaN])('rejects a discount of %s', (discountPercentage) => {
    expect(issuesOf(discountCodeSchema, { code: 'SAVE10', discountPercentage })).toEqual([
      { path: 'discountPercentage', message: 'Discount must be a whole number between 1 and 100' }
    ])
  })
})
