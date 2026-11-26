import { screen } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { Route, Routes } from 'react-router-dom'
import { PublicRoute } from '@/layouts/PublicRoute'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'
import { renderWithProviders } from '@/test/render'

const initialState = useAuthStore.getInitialState()

const renderRoute = (entry: Parameters<typeof renderWithProviders>[1] = ['/login']) =>
  renderWithProviders(
    <Routes>
      <Route path="/" element={<p>Home page</p>} />
      <Route path="/orders" element={<p>Orders page</p>} />
      <Route element={<PublicRoute />}>
        <Route path="/login" element={<p>Login page</p>} />
      </Route>
    </Routes>,
    entry
  )

describe('PublicRoute', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
  })

  it('shows the page to a visitor', () => {
    renderRoute()

    expect(screen.getByText('Login page')).toBeInTheDocument()
  })

  it('shows the page when there is a user but no access token', () => {
    useAuthStore.setState({ user: buildUser() })

    renderRoute()

    expect(screen.getByText('Login page')).toBeInTheDocument()
  })

  it('sends an authenticated user to the home page', () => {
    useAuthStore.getState().setAuth(buildUser(), 'token')

    renderRoute()

    expect(screen.getByText('Home page')).toBeInTheDocument()
  })

  it('sends an authenticated user back to the page they came from', () => {
    useAuthStore.getState().setAuth(buildUser(), 'token')

    renderRoute([{ pathname: '/login', state: { from: { pathname: '/orders' } } }])

    expect(screen.getByText('Orders page')).toBeInTheDocument()
  })
})
