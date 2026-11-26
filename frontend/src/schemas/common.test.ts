import { describe, expect, it } from 'vitest'
import { emailSchema, nameSchema, passwordSchema } from '@/schemas/common'
import { issuesOf } from '@/test/schema'

describe('nameSchema', () => {
  const schema = nameSchema('First name')

  it('trims surrounding spaces', () => {
    expect(schema.parse('  Ana ')).toBe('Ana')
  })

  it.each(['', '   '])('rejects %j with the label in the message', (value) => {
    expect(issuesOf(schema, value)).toEqual([{ path: '', message: 'First name is required' }])
  })

  it('accepts exactly 100 characters', () => {
    expect(issuesOf(schema, 'a'.repeat(100))).toEqual([])
  })

  it('rejects more than 100 characters', () => {
    expect(issuesOf(schema, 'a'.repeat(101))).toEqual([
      { path: '', message: 'First name must be at most 100 characters' }
    ])
  })
})

describe('emailSchema', () => {
  it('accepts a valid email and trims it', () => {
    expect(emailSchema.parse('  ana@mail.com ')).toBe('ana@mail.com')
  })

  it('rejects an empty email', () => {
    expect(issuesOf(emailSchema, '')).toContainEqual({ path: '', message: 'Email is required' })
  })

  it.each(['abc', 'ana@', '@mail.com', 'ana mail.com'])('rejects the invalid format %j', (value) => {
    expect(issuesOf(emailSchema, value)).toContainEqual({ path: '', message: 'Invalid email format' })
  })

  it('rejects more than 254 characters', () => {
    expect(issuesOf(emailSchema, `${'a'.repeat(250)}@x.com`)).toContainEqual({
      path: '',
      message: 'Email must be at most 254 characters'
    })
  })
})

describe('passwordSchema', () => {
  const LENGTH_MESSAGE = 'Password must be between 8 and 72 characters long'
  const STRENGTH_MESSAGE = 'Password must contain uppercase, lowercase and number'

  it.each([8, 72])('accepts a valid password with %s characters', (length) => {
    expect(issuesOf(passwordSchema, 'Abcdefg1'.padEnd(length, 'x'))).toEqual([])
  })

  it('rejects an empty password', () => {
    expect(issuesOf(passwordSchema, '')).toContainEqual({ path: '', message: 'Password is required' })
  })

  it('rejects fewer than 8 characters', () => {
    expect(issuesOf(passwordSchema, 'Ab1')).toEqual([{ path: '', message: LENGTH_MESSAGE }])
  })

  it('rejects more than 72 characters', () => {
    expect(issuesOf(passwordSchema, 'Abcdefg1'.padEnd(73, 'x'))).toEqual([{ path: '', message: LENGTH_MESSAGE }])
  })

  it.each([
    ['uppercase letter', 'abcdefg1'],
    ['lowercase letter', 'ABCDEFG1'],
    ['digit', 'Abcdefgh']
  ])('rejects a password without %s', (_, password) => {
    expect(issuesOf(passwordSchema, password)).toEqual([{ path: '', message: STRENGTH_MESSAGE }])
  })
})
