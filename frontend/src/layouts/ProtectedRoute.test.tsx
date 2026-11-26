import { screen } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { Route, Routes, useLocation } from 'react-router-dom'
import { ProtectedRoute } from '@/layouts/ProtectedRoute'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'
import { renderWithProviders } from '@/test/render'
import type { Permission } from '@/types/user'

const initialState = useAuthStore.getInitialState()

interface LocationState {
  from?: { pathname: string }
}

const LoginProbe = () => {
  const location = useLocation()

  return <p>Login (came from {(location.state as LocationState | null)?.from?.pathname})</p>
}

const renderRoute = (permission?: Permission) =>
  renderWithProviders(
    <Routes>
      <Route path="/login" element={<LoginProbe />} />
      <Route path="/no-permission" element={<p>No permission</p>} />
      <Route element={<ProtectedRoute permission={permission} />}>
        <Route path="/admin" element={<p>Admin page</p>} />
      </Route>
    </Routes>,
    ['/admin']
  )

describe('ProtectedRoute', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
  })

  it('sends a visitor to the login and remembers where they came from', () => {
    renderRoute()

    expect(screen.getByText('Login (came from /admin)')).toBeInTheDocument()
  })

  it('sends to the login when there is a user but no access token', () => {
    useAuthStore.setState({ user: buildUser() })

    renderRoute()

    expect(screen.getByText(/^Login/)).toBeInTheDocument()
  })

  it('shows the page to an authenticated user when no permission is required', () => {
    useAuthStore.getState().setAuth(buildUser(), 'token')

    renderRoute()

    expect(screen.getByText('Admin page')).toBeInTheDocument()
  })

  it('shows the page when the user has the required permission', () => {
    useAuthStore.getState().setAuth(buildUser({ permissions: ['DASHBOARD_READ'] }), 'token')

    renderRoute('DASHBOARD_READ')

    expect(screen.getByText('Admin page')).toBeInTheDocument()
  })

  it('sends to no-permission when the user lacks the required permission', () => {
    useAuthStore.getState().setAuth(buildUser({ permissions: ['PRODUCTS_READ'] }), 'token')

    renderRoute('DASHBOARD_READ')

    expect(screen.getByText('No permission')).toBeInTheDocument()
  })
})
