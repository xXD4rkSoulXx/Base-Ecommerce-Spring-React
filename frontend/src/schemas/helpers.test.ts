import { describe, expect, it } from 'vitest'
import { selectSchema, wholeNumberSchema } from '@/schemas/helpers'
import { issuesOf } from '@/test/schema'

const MESSAGE = 'Invalid value'

describe('wholeNumberSchema', () => {
  const schema = wholeNumberSchema(MESSAGE, 1, 10)

  it.each([1, 5, 10])('accepts %j', (value) => {
    expect(issuesOf(schema, value)).toEqual([])
  })

  it.each([0, 11, 1.5, NaN, '5', undefined])('rejects %j with the given message', (value) => {
    expect(issuesOf(schema, value)).toEqual([{ path: '', message: MESSAGE }])
  })
})

describe('selectSchema', () => {
  const schema = selectSchema<'A' | 'B'>(MESSAGE)

  it('accepts a non-empty string', () => {
    expect(issuesOf(schema, 'A')).toEqual([])
  })

  it.each(['', 5, undefined])('rejects %j with the given message', (value) => {
    expect(issuesOf(schema, value)).toEqual([{ path: '', message: MESSAGE }])
  })
})
