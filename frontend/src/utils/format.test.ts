import { describe, expect, it } from 'vitest'
import {
  formatCategory,
  formatCompactDateTime,
  formatCountry,
  formatDate,
  formatDateTime,
  formatNumber,
  formatPercentChange,
  formatPrice,
  formatRating,
  formatSize,
  pluralize
} from '@/utils/format'

const normalizeSpaces = (value: string) => value.replace(/\s/g, ' ')

describe('formatPrice', () => {
  it('formats euros with the pt-PT decimal comma and the currency after the value', () => {
    expect(normalizeSpaces(formatPrice(1234.5))).toBe('1234,50 €')
  })

  it('formats zero with two decimals', () => {
    expect(normalizeSpaces(formatPrice(0))).toBe('0,00 €')
  })

  it('formats negative values', () => {
    expect(normalizeSpaces(formatPrice(-5))).toBe('-5,00 €')
  })
})

describe('formatDate', () => {
  it('formats an ISO date as day/month/year', () => {
    expect(formatDate('2026-03-15T12:00:00Z')).toBe('15/03/2026')
  })
})

describe('formatDateTime', () => {
  it('formats date and time in the configured timezone', () => {
    expect(formatDateTime('2026-03-15T14:30:00Z')).toMatch(/^15\/03\/2026.*14:30$/)
  })
})

describe('formatCompactDateTime', () => {
  it('formats date and time with a two-digit year', () => {
    expect(formatCompactDateTime('2026-03-15T14:30:00Z')).toMatch(/^15\/03\/26.*14:30$/)
  })
})

describe('formatPercentChange', () => {
  it('shows the sign of positive changes and rounds to one decimal', () => {
    expect(formatPercentChange(12.34)).toBe('+12,3')
  })

  it('shows the sign of negative changes', () => {
    expect(formatPercentChange(-5)).toBe('-5')
  })

  it('shows no sign for zero', () => {
    expect(formatPercentChange(0)).toBe('0')
  })

  it('shows no sign for a change that rounds to zero', () => {
    expect(formatPercentChange(0.04)).toBe('0')
  })
})

describe('formatNumber', () => {
  it('groups thousands with a space', () => {
    expect(normalizeSpaces(formatNumber(1234567))).toBe('1 234 567')
  })

  it('does not group four-digit numbers (pt-PT rule)', () => {
    expect(formatNumber(1234)).toBe('1234')
  })
})

describe('formatRating', () => {
  it('uses a decimal comma and the scale out of five', () => {
    expect(formatRating(4.25)).toBe('4,3 / 5')
  })

  it('does not show decimals for a whole rating', () => {
    expect(formatRating(4)).toBe('4 / 5')
  })
})

describe('formatCountry', () => {
  it('returns the English name of the country', () => {
    expect(formatCountry('PT')).toBe('Portugal')
    expect(formatCountry('DE')).toBe('Germany')
  })
})

describe('formatSize', () => {
  it('shows ONE_SIZE as text', () => {
    expect(formatSize('ONE_SIZE')).toBe('One size')
  })

  it('removes the underscore of numeric sizes', () => {
    expect(formatSize('_42')).toBe('42')
  })

  it('keeps letter sizes as they are', () => {
    expect(formatSize('M')).toBe('M')
  })
})

describe('formatCategory', () => {
  it('capitalizes only the first letter', () => {
    expect(formatCategory('OUTERWEAR')).toBe('Outerwear')
    expect(formatCategory('TOPS')).toBe('Tops')
  })
})

describe('pluralize', () => {
  it('uses the singular for exactly one', () => {
    expect(pluralize(1, 'item')).toBe('1 item')
  })

  it('uses the plural for zero and for more than one', () => {
    expect(pluralize(0, 'item')).toBe('0 items')
    expect(pluralize(2, 'item')).toBe('2 items')
  })
})
