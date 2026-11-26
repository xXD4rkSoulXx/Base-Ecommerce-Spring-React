import { describe, expect, it } from 'vitest'
import { productSchema } from '@/schemas/admin/adminProduct'
import { issuesOf } from '@/test/schema'

const PRICE_MESSAGE = 'Price must be between 0.01 and 99999.99, with at most 2 decimal places'

const validProduct = {
  ref: 'REF-1',
  name: 'Rain Jacket',
  description: 'Waterproof jacket.',
  price: '49.90',
  discountPercentage: 0,
  category: 'OUTERWEAR',
  featuredSlot: ''
}

describe('productSchema', () => {
  it('accepts a valid product', () => {
    expect(issuesOf(productSchema, validProduct)).toEqual([])
  })

  describe('ref', () => {
    it('trims and uppercases', () => {
      expect(productSchema.parse({ ...validProduct, ref: '  ab-12 ' }).ref).toBe('AB-12')
    })

    it('is required', () => {
      expect(issuesOf(productSchema, { ...validProduct, ref: '  ' })).toContainEqual({
        path: 'ref',
        message: 'Ref is required'
      })
    })

    it.each(['AB 1', 'AB_1', 'AB#1'])('rejects the characters in %j', (ref) => {
      expect(issuesOf(productSchema, { ...validProduct, ref })).toEqual([
        { path: 'ref', message: 'Ref can only contain letters, numbers and dashes' }
      ])
    })

    it('rejects more than 50 characters', () => {
      expect(issuesOf(productSchema, { ...validProduct, ref: 'A'.repeat(51) })).toEqual([
        { path: 'ref', message: 'Ref must be at most 50 characters' }
      ])
    })
  })

  describe('name and description', () => {
    it('requires a name', () => {
      expect(issuesOf(productSchema, { ...validProduct, name: ' ' })).toEqual([
        { path: 'name', message: 'Name is required' }
      ])
    })

    it('rejects a name with more than 120 characters', () => {
      expect(issuesOf(productSchema, { ...validProduct, name: 'x'.repeat(121) })).toEqual([
        { path: 'name', message: 'Name must be at most 120 characters' }
      ])
    })

    it('requires a description', () => {
      expect(issuesOf(productSchema, { ...validProduct, description: '' })).toEqual([
        { path: 'description', message: 'Description is required' }
      ])
    })

    it('accepts a description with exactly 5000 characters', () => {
      expect(issuesOf(productSchema, { ...validProduct, description: 'x'.repeat(5000) })).toEqual([])
    })

    it('rejects a description with more than 5000 characters', () => {
      expect(issuesOf(productSchema, { ...validProduct, description: 'x'.repeat(5001) })).toEqual([
        { path: 'description', message: 'Description must be at most 5000 characters' }
      ])
    })
  })

  describe('price', () => {
    it.each(['0.01', '1', '10,5', '49.90', '99999.99', ' 12.3 '])('accepts %j', (price) => {
      expect(issuesOf(productSchema, { ...validProduct, price })).toEqual([])
    })

    it.each(['0', '0.00', '0,0'])('rejects %j because it is below 0.01', (price) => {
      expect(issuesOf(productSchema, { ...validProduct, price })).toEqual([{ path: 'price', message: PRICE_MESSAGE }])
    })

    it.each(['', 'abc', '-1', '100000', '1.234', '1.', '.5', '1,2,3'])('rejects %j', (price) => {
      expect(issuesOf(productSchema, { ...validProduct, price })).toContainEqual({
        path: 'price',
        message: PRICE_MESSAGE
      })
    })
  })

  describe('discountPercentage', () => {
    it.each([0, 100])('accepts %s', (discountPercentage) => {
      expect(issuesOf(productSchema, { ...validProduct, discountPercentage })).toEqual([])
    })

    it.each([-1, 101, 5.5])('rejects %s', (discountPercentage) => {
      expect(issuesOf(productSchema, { ...validProduct, discountPercentage })).toEqual([
        { path: 'discountPercentage', message: 'Discount must be a whole number between 0 and 100' }
      ])
    })
  })

  it('requires a category', () => {
    expect(issuesOf(productSchema, { ...validProduct, category: '' })).toEqual([
      { path: 'category', message: 'Select a category' }
    ])
  })

  it.each(['', 'LEFT', 'RIGHT'])('accepts the featured slot %j', (featuredSlot) => {
    expect(issuesOf(productSchema, { ...validProduct, featuredSlot })).toEqual([])
  })

  it('rejects an unknown featured slot', () => {
    expect(issuesOf(productSchema, { ...validProduct, featuredSlot: 'CENTER' }).map((issue) => issue.path)).toEqual([
      'featuredSlot'
    ])
  })
})
