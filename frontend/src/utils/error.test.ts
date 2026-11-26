import { AxiosError, AxiosHeaders } from 'axios'
import { describe, expect, it } from 'vitest'
import { getErrorMessage } from '@/utils/error'

const FALLBACK = 'Something went wrong'

const badRequest = (data: unknown) =>
  new AxiosError('Request failed', AxiosError.ERR_BAD_REQUEST, undefined, undefined, {
    data,
    status: 400,
    statusText: 'Bad Request',
    headers: {},
    config: { headers: new AxiosHeaders() }
  })

describe('getErrorMessage', () => {
  it('returns the message sent by the server', () => {
    expect(getErrorMessage(badRequest({ message: 'Email already in use' }), FALLBACK)).toBe('Email already in use')
  })

  it('returns the fallback when the response has no message', () => {
    expect(getErrorMessage(badRequest({}), FALLBACK)).toBe(FALLBACK)
  })

  it('returns the fallback when there is no response at all', () => {
    expect(getErrorMessage(new AxiosError('Network Error'), FALLBACK)).toBe(FALLBACK)
  })

  it('returns the fallback for errors that do not come from axios', () => {
    expect(getErrorMessage(new Error('boom'), FALLBACK)).toBe(FALLBACK)
    expect(getErrorMessage('boom', FALLBACK)).toBe(FALLBACK)
    expect(getErrorMessage(undefined, FALLBACK)).toBe(FALLBACK)
  })
})
