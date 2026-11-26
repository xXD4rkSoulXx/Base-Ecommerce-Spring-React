import { describe, expect, it } from 'vitest'
import { assignRoleSchema } from '@/schemas/admin/adminTeam'
import { issuesOf } from '@/test/schema'

describe('assignRoleSchema', () => {
  const schema = assignRoleSchema('CUSTOMER')

  it('accepts a valid email with a different role', () => {
    expect(issuesOf(schema, { email: 'ana@mail.com', role: 'SUPPORT' })).toEqual([])
  })

  it('trims the email', () => {
    expect(schema.parse({ email: '  ana@mail.com ', role: 'SUPPORT' }).email).toBe('ana@mail.com')
  })

  it('rejects the role the user already has', () => {
    expect(issuesOf(schema, { email: 'ana@mail.com', role: 'CUSTOMER' })).toEqual([
      { path: 'role', message: 'Select a different role' }
    ])
  })

  it('rejects an invalid email', () => {
    expect(issuesOf(schema, { email: 'abc', role: 'SUPPORT' })).toContainEqual({
      path: 'email',
      message: 'Invalid email format'
    })
  })

  it('accepts any role when there is no current role', () => {
    expect(issuesOf(assignRoleSchema(), { email: 'ana@mail.com', role: 'SUPPORT' })).toEqual([])
  })

  it('rejects a missing role', () => {
    expect(issuesOf(assignRoleSchema(), { email: 'ana@mail.com' }).map((issue) => issue.path)).toEqual(['role'])
  })
})
