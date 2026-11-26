import { describe, expect, it } from 'vitest'
import { profileSchema } from '@/schemas/profile'
import { issuesOf } from '@/test/schema'

describe('profileSchema', () => {
  it('accepts a valid profile', () => {
    expect(issuesOf(profileSchema, { firstName: 'Ana', lastName: 'Costa' })).toEqual([])
  })

  it('requires the first name', () => {
    expect(issuesOf(profileSchema, { firstName: ' ', lastName: 'Costa' })).toEqual([
      { path: 'firstName', message: 'First name is required' }
    ])
  })

  it('requires the last name', () => {
    expect(issuesOf(profileSchema, { firstName: 'Ana', lastName: '' })).toEqual([
      { path: 'lastName', message: 'Last name is required' }
    ])
  })
})
