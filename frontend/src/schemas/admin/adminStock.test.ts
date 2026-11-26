import { describe, expect, it } from 'vitest'
import { stockSchema } from '@/schemas/admin/adminStock'
import { issuesOf } from '@/test/schema'

const AMOUNT_MESSAGE = 'Enter a whole number between -100000 and 100000'
const BELOW_ZERO = { path: 'amount', message: 'Stock cannot go below zero' }

describe('stockSchema', () => {
  const schema = stockSchema(5)

  describe('ADJUST mode', () => {
    it.each([-5, 0, 10, 100000])('accepts adding %s to a stock of 5', (amount) => {
      expect(issuesOf(schema, { mode: 'ADJUST', amount })).toEqual([])
    })

    it('rejects an adjustment that would leave the stock below zero', () => {
      expect(issuesOf(schema, { mode: 'ADJUST', amount: -6 })).toEqual([BELOW_ZERO])
    })
  })

  describe('SET mode', () => {
    it.each([0, 3, 100000])('accepts setting the stock to %s', (amount) => {
      expect(issuesOf(schema, { mode: 'SET', amount })).toEqual([])
    })

    it('rejects a negative target stock, whatever the current stock is', () => {
      expect(issuesOf(schema, { mode: 'SET', amount: -1 })).toEqual([BELOW_ZERO])
    })
  })

  it('uses the current stock it was created with', () => {
    expect(issuesOf(stockSchema(100), { mode: 'ADJUST', amount: -100 })).toEqual([])
    expect(issuesOf(stockSchema(100), { mode: 'ADJUST', amount: -101 })).toEqual([BELOW_ZERO])
  })

  it('rejects an amount above 100000', () => {
    expect(issuesOf(schema, { mode: 'ADJUST', amount: 100001 })).toEqual([{ path: 'amount', message: AMOUNT_MESSAGE }])
  })

  it('rejects an amount below -100000', () => {
    expect(issuesOf(schema, { mode: 'ADJUST', amount: -100001 })).toContainEqual({
      path: 'amount',
      message: AMOUNT_MESSAGE
    })
  })

  it('rejects a decimal amount', () => {
    expect(issuesOf(schema, { mode: 'ADJUST', amount: 1.5 })).toContainEqual({
      path: 'amount',
      message: AMOUNT_MESSAGE
    })
  })

  it('rejects a missing amount', () => {
    expect(issuesOf(schema, { mode: 'ADJUST' })).toContainEqual({ path: 'amount', message: AMOUNT_MESSAGE })
  })

  it('rejects an unknown mode', () => {
    expect(issuesOf(schema, { mode: 'REMOVE', amount: 1 }).map((issue) => issue.path)).toContain('mode')
  })
})
