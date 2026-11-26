import { beforeEach, describe, expect, it } from 'vitest'
import { clearSessionHint, hasSessionHint, markSessionHint } from '@/lib/sessionHint'

beforeEach(() => {
  localStorage.clear()
})

describe('sessionHint', () => {
  it('starts without a hint', () => {
    expect(hasSessionHint()).toBe(false)
  })

  it('remembers the hint once marked', () => {
    markSessionHint()

    expect(hasSessionHint()).toBe(true)
  })

  it('forgets the hint once cleared', () => {
    markSessionHint()
    clearSessionHint()

    expect(hasSessionHint()).toBe(false)
  })
})
