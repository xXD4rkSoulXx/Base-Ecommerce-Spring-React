import { render } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it } from 'vitest'
import { APP_NAME } from '@/constants'
import { useRouteTitle } from '@/hooks/useRouteTitle'

const Probe = () => {
  useRouteTitle()

  return null
}

const renderAt = (path: string) => {
  const router = createMemoryRouter(
    [
      {
        path: '/',
        element: <Probe />,
        handle: { title: 'Home' },
        children: [
          { path: 'orders', handle: { title: 'Orders' }, element: null },
          { path: 'untitled', element: null }
        ]
      }
    ],
    { initialEntries: [path] }
  )

  render(<RouterProvider router={router} />)
}

describe('useRouteTitle', () => {
  beforeEach(() => {
    document.title = 'initial'
  })

  it('uses the title of the parent route', () => {
    renderAt('/')

    expect(document.title).toBe(`Home - ${APP_NAME}`)
  })

  it('prefers the title of the deepest route', () => {
    renderAt('/orders')

    expect(document.title).toBe(`Orders - ${APP_NAME}`)
  })

  it('falls back to the closest ancestor with a title', () => {
    renderAt('/untitled')

    expect(document.title).toBe(`Home - ${APP_NAME}`)
  })
})
