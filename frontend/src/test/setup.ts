import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import axios from 'axios'
import { afterAll, afterEach, beforeAll } from 'vitest'
import { server } from '@/test/server'

axios.defaults.adapter = 'fetch'

beforeAll(() => {
  server.listen({ onUnhandledFrame: 'error' })
})

afterEach(() => {
  cleanup()
  server.resetHandlers()
})

afterAll(() => {
  server.close()
})
