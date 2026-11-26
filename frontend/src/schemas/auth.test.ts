import { describe, expect, it } from 'vitest'
import { changePasswordSchema, loginSchema, registerSchema } from '@/schemas/auth'
import { issuesOf } from '@/test/schema'

const validRegistration = {
  firstName: 'Ana',
  lastName: 'Costa',
  email: 'ana@mail.com',
  password: 'Abcdefg1',
  confirmPassword: 'Abcdefg1',
  accept: true
}

const validPasswordChange = {
  currentPassword: 'Old12345',
  newPassword: 'Abcdefg1',
  confirmPassword: 'Abcdefg1'
}

describe('registerSchema', () => {
  it('accepts a valid registration', () => {
    expect(issuesOf(registerSchema, validRegistration)).toEqual([])
  })

  it('rejects a confirmation that does not match the password', () => {
    expect(issuesOf(registerSchema, { ...validRegistration, confirmPassword: 'Abcdefg2' })).toContainEqual({
      path: 'confirmPassword',
      message: 'Passwords do not match.'
    })
  })

  it('rejects when the protocol is not accepted', () => {
    expect(issuesOf(registerSchema, { ...validRegistration, accept: false })).toContainEqual({
      path: 'accept',
      message: 'You must accept the Network Protocol to continue.'
    })
  })

  it('rejects an invalid email', () => {
    expect(issuesOf(registerSchema, { ...validRegistration, email: 'abc' })).toContainEqual({
      path: 'email',
      message: 'Invalid email format'
    })
  })

  it('rejects a weak password', () => {
    const weak = { ...validRegistration, password: 'abc', confirmPassword: 'abc' }

    expect(issuesOf(registerSchema, weak)).toContainEqual({
      path: 'password',
      message: 'Password must be between 8 and 72 characters long'
    })
  })
})

describe('loginSchema', () => {
  it('accepts a valid login', () => {
    expect(issuesOf(loginSchema, { email: 'ana@mail.com', password: 'Abcdefg1' })).toEqual([])
  })

  it('rejects an invalid email', () => {
    expect(issuesOf(loginSchema, { email: 'abc', password: 'Abcdefg1' })).toContainEqual({
      path: 'email',
      message: 'Invalid email format'
    })
  })

  it('rejects an empty password', () => {
    expect(issuesOf(loginSchema, { email: 'ana@mail.com', password: '' })).toEqual([
      { path: 'password', message: 'Password is required' }
    ])
  })

  it('does not apply the strength rules, so older accounts can still log in', () => {
    expect(issuesOf(loginSchema, { email: 'ana@mail.com', password: 'abc' })).toEqual([])
  })
})

describe('changePasswordSchema', () => {
  it('accepts a valid change', () => {
    expect(issuesOf(changePasswordSchema, validPasswordChange)).toEqual([])
  })

  it('rejects an empty current password', () => {
    expect(issuesOf(changePasswordSchema, { ...validPasswordChange, currentPassword: '' })).toContainEqual({
      path: 'currentPassword',
      message: 'Current password is required'
    })
  })

  it('rejects a new password equal to the current one', () => {
    const same = { currentPassword: 'Abcdefg1', newPassword: 'Abcdefg1', confirmPassword: 'Abcdefg1' }

    expect(issuesOf(changePasswordSchema, same)).toEqual([
      { path: 'newPassword', message: 'New password must be different from the current password' }
    ])
  })

  it('rejects a confirmation that does not match', () => {
    expect(issuesOf(changePasswordSchema, { ...validPasswordChange, confirmPassword: 'Abcdefg2' })).toEqual([
      { path: 'confirmPassword', message: 'Passwords do not match' }
    ])
  })

  it('rejects a weak new password', () => {
    const weak = { ...validPasswordChange, newPassword: 'abc', confirmPassword: 'abc' }

    expect(issuesOf(changePasswordSchema, weak)).toContainEqual({
      path: 'newPassword',
      message: 'Password must contain uppercase, lowercase and number'
    })
  })
})
