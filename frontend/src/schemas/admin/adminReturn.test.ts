import { describe, expect, it } from 'vitest'
import { rejectReturnSchema } from '@/schemas/admin/adminReturn'
import { issuesOf } from '@/test/schema'

describe('rejectReturnSchema', () => {
  it('accepts a reason and trims it', () => {
    expect(rejectReturnSchema.parse({ reason: '  Item was used ' }).reason).toBe('Item was used')
  })

  it.each(['', '   '])('rejects the blank reason %j', (reason) => {
    expect(issuesOf(rejectReturnSchema, { reason })).toEqual([{ path: 'reason', message: 'Reason is required' }])
  })

  it('accepts exactly 255 characters', () => {
    expect(issuesOf(rejectReturnSchema, { reason: 'x'.repeat(255) })).toEqual([])
  })

  it('rejects more than 255 characters', () => {
    expect(issuesOf(rejectReturnSchema, { reason: 'x'.repeat(256) })).toEqual([
      { path: 'reason', message: 'Reason must be at most 255 characters' }
    ])
  })
})
