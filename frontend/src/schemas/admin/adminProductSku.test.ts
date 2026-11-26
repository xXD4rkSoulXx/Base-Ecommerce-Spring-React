import { describe, expect, it } from 'vitest'
import { variantSchema } from '@/schemas/admin/adminProductSku'
import { issuesOf } from '@/test/schema'

const validVariant = {
  size: 'M',
  color: 'BLACK',
  weightGrams: 500,
  lengthCm: 30,
  widthCm: 20,
  heightCm: 5,
  discountPercentage: 0
}

describe('variantSchema', () => {
  it('accepts a valid variant', () => {
    expect(issuesOf(variantSchema, validVariant)).toEqual([])
  })

  it('requires a size', () => {
    expect(issuesOf(variantSchema, { ...validVariant, size: '' })).toEqual([{ path: 'size', message: 'Select a size' }])
  })

  it('requires a color', () => {
    expect(issuesOf(variantSchema, { ...validVariant, color: '' })).toEqual([
      { path: 'color', message: 'Select a color' }
    ])
  })

  it.each([
    ['weightGrams', 100000, 'Weight must be a whole number between 1 and 100000 g'],
    ['lengthCm', 1000, 'Length must be a whole number between 1 and 1000 cm'],
    ['widthCm', 1000, 'Width must be a whole number between 1 and 1000 cm'],
    ['heightCm', 1000, 'Height must be a whole number between 1 and 1000 cm']
  ])('limits %s to between 1 and %s', (field, max, message) => {
    expect(issuesOf(variantSchema, { ...validVariant, [field]: 1 })).toEqual([])
    expect(issuesOf(variantSchema, { ...validVariant, [field]: max })).toEqual([])
    expect(issuesOf(variantSchema, { ...validVariant, [field]: 0 })).toEqual([{ path: field, message }])
    expect(issuesOf(variantSchema, { ...validVariant, [field]: Number(max) + 1 })).toEqual([{ path: field, message }])
    expect(issuesOf(variantSchema, { ...validVariant, [field]: 1.5 })).toEqual([{ path: field, message }])
  })

  it.each([0, 100])('accepts a discount of %s', (discountPercentage) => {
    expect(issuesOf(variantSchema, { ...validVariant, discountPercentage })).toEqual([])
  })

  it.each([-1, 101, 2.5])('rejects a discount of %s', (discountPercentage) => {
    expect(issuesOf(variantSchema, { ...validVariant, discountPercentage })).toEqual([
      { path: 'discountPercentage', message: 'Discount must be a whole number between 0 and 100' }
    ])
  })
})
