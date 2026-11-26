import { renderHook, waitFor } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { expect } from 'vitest'
import { createRouterWrapper } from '@/test/render'
import { apiUrl, server } from '@/test/server'

export const captureRequestParams = async (path: string, useHook: () => unknown, entry: string) => {
  let params: Record<string, string> | undefined

  server.use(
    http.get(apiUrl(path), ({ request }) => {
      params = Object.fromEntries(new URL(request.url).searchParams)

      return HttpResponse.json({ content: [] })
    })
  )
  renderHook(useHook, { wrapper: createRouterWrapper([entry]) })

  await waitFor(() => expect(params).toBeDefined())

  return params
}
