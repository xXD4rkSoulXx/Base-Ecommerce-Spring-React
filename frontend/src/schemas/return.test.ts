import { describe, expect, it } from 'vitest'
import { returnSchema } from '@/schemas/return'
import { issuesOf } from '@/test/schema'

const validReturn = { quantities: { '1': 1 }, reason: 'DEFECTIVE', comment: '' }

describe('returnSchema', () => {
  it('accepts a valid return', () => {
    expect(issuesOf(returnSchema, validReturn)).toEqual([])
  })

  it.each(['DOES_NOT_FIT', 'DEFECTIVE', 'NOT_AS_DESCRIBED', 'CHANGED_MIND'])(
    'accepts the reason %s without a comment',
    (reason) => {
      expect(issuesOf(returnSchema, { ...validReturn, reason })).toEqual([])
    }
  )

  it('requires at least one item when every quantity is zero', () => {
    expect(issuesOf(returnSchema, { ...validReturn, quantities: { '1': 0, '2': 0 } })).toEqual([
      { path: 'quantities', message: 'Select at least one item' }
    ])
  })

  it('requires at least one item when there are no quantities', () => {
    expect(issuesOf(returnSchema, { ...validReturn, quantities: {} })).toEqual([
      { path: 'quantities', message: 'Select at least one item' }
    ])
  })

  it('rejects a negative quantity', () => {
    expect(issuesOf(returnSchema, { ...validReturn, quantities: { '1': -1 } })).toContainEqual({
      path: 'quantities.1',
      message: expect.any(String)
    })
  })

  it('requires a reason', () => {
    expect(issuesOf(returnSchema, { ...validReturn, reason: '' })).toEqual([
      { path: 'reason', message: 'Select a reason' }
    ])
  })

  it('requires a comment when the reason is OTHER', () => {
    expect(issuesOf(returnSchema, { ...validReturn, reason: 'OTHER', comment: '   ' })).toEqual([
      { path: 'comment', message: 'Please describe the reason' }
    ])
  })

  it('accepts the reason OTHER with a comment', () => {
    expect(issuesOf(returnSchema, { ...validReturn, reason: 'OTHER', comment: 'Wrong color' })).toEqual([])
  })

  it('rejects a comment with more than 500 characters', () => {
    expect(issuesOf(returnSchema, { ...validReturn, comment: 'x'.repeat(501) })).toEqual([
      { path: 'comment', message: 'Comment cannot exceed 500 characters' }
    ])
  })
})
