import { describe, expect, it } from 'vitest'
import { checkoutAddressSchema, promoCodeSchema } from '@/schemas/checkout'
import { issuesOf } from '@/test/schema'

const validAddress = {
  recipientName: 'Ana Costa',
  addressLine1: 'Rua A 1',
  addressLine2: '2º Esq',
  city: 'Lisboa',
  state: 'Lisboa',
  postalCode: '1000-001',
  country: 'PT',
  phoneCountry: 'PT',
  phone: '912345678'
}

describe('checkoutAddressSchema', () => {
  it('accepts a valid address', () => {
    expect(issuesOf(checkoutAddressSchema, validAddress)).toEqual([])
  })

  it('accepts an empty address line 2 and an empty state', () => {
    expect(issuesOf(checkoutAddressSchema, { ...validAddress, addressLine2: '', state: '' })).toEqual([])
  })

  it('trims surrounding spaces', () => {
    expect(checkoutAddressSchema.parse({ ...validAddress, city: '  Lisboa ' }).city).toBe('Lisboa')
  })

  it.each(['recipientName', 'addressLine1', 'city', 'postalCode'])('requires %s', (field) => {
    expect(issuesOf(checkoutAddressSchema, { ...validAddress, [field]: '   ' })).toContainEqual({
      path: field,
      message: expect.stringContaining('is required')
    })
  })

  it.each(['pt', 'PRT', 'P', '', 'P1'])('rejects the country %j', (country) => {
    expect(issuesOf(checkoutAddressSchema, { ...validAddress, country })).toContainEqual({
      path: 'country',
      message: 'Country must be a 2-letter code, e.g. PT'
    })
  })

  it.each([
    ['recipientName', 101],
    ['addressLine1', 101],
    ['addressLine2', 101],
    ['city', 101],
    ['state', 101],
    ['postalCode', 21]
  ])('rejects a %s longer than the limit (%s characters)', (field, length) => {
    expect(issuesOf(checkoutAddressSchema, { ...validAddress, [field]: 'x'.repeat(Number(length)) })).toContainEqual({
      path: field,
      message: expect.stringContaining('at most')
    })
  })

  describe('phone', () => {
    it('requires a phone number', () => {
      expect(issuesOf(checkoutAddressSchema, { ...validAddress, phone: '' })).toEqual([
        { path: 'phone', message: 'Phone is required' }
      ])
    })

    it('rejects more than 15 digits', () => {
      expect(issuesOf(checkoutAddressSchema, { ...validAddress, phone: '9'.repeat(16) })).toContainEqual({
        path: 'phone',
        message: 'Phone must be at most 15 digits'
      })
    })

    it('rejects a number that is not valid for the dial code country', () => {
      expect(issuesOf(checkoutAddressSchema, { ...validAddress, phone: '123' })).toEqual([
        { path: 'phone', message: 'Enter a valid phone number' }
      ])
    })

    it('validates the number against the selected dial code', () => {
      expect(issuesOf(checkoutAddressSchema, { ...validAddress, phoneCountry: 'US' })).toEqual([
        { path: 'phone', message: 'Enter a valid phone number' }
      ])
    })
  })

  describe('postal code', () => {
    it('rejects a postal code with the wrong format for the country', () => {
      expect(issuesOf(checkoutAddressSchema, { ...validAddress, postalCode: 'ABC' })).toEqual([
        { path: 'postalCode', message: 'Enter a valid postal code for this country' }
      ])
    })

    it('accepts a valid postal code for another country', () => {
      expect(issuesOf(checkoutAddressSchema, { ...validAddress, country: 'US', postalCode: '90210' })).toEqual([])
    })

    it('does not validate the format when the country has no known postal code rules', () => {
      expect(issuesOf(checkoutAddressSchema, { ...validAddress, country: 'XX', postalCode: 'anything' })).toEqual([])
    })
  })
})

describe('promoCodeSchema', () => {
  it('accepts a code and trims it', () => {
    expect(promoCodeSchema.parse({ code: ' SAVE10 ' }).code).toBe('SAVE10')
  })

  it('rejects a blank code', () => {
    expect(issuesOf(promoCodeSchema, { code: '  ' })).toEqual([{ path: 'code', message: 'Enter a promo code' }])
  })

  it('rejects a code with more than 20 characters', () => {
    expect(issuesOf(promoCodeSchema, { code: 'x'.repeat(21) })).toEqual([
      { path: 'code', message: 'Invalid discount code' }
    ])
  })
})
