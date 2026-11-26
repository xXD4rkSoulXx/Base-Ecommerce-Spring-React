import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import type { ReactElement, ReactNode } from 'react'
import { MemoryRouter, type MemoryRouterProps } from 'react-router-dom'

export const createTestQueryClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } })

export const createWrapper = () => {
  const queryClient = createTestQueryClient()

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  )
}

export const createRouterWrapper = (initialEntries: MemoryRouterProps['initialEntries'] = ['/']) => {
  const queryClient = createTestQueryClient()

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={initialEntries}>{children}</MemoryRouter>
    </QueryClientProvider>
  )
}

export const renderWithProviders = (ui: ReactElement, initialEntries: MemoryRouterProps['initialEntries'] = ['/']) =>
  render(
    <QueryClientProvider client={createTestQueryClient()}>
      <MemoryRouter initialEntries={initialEntries}>{ui}</MemoryRouter>
    </QueryClientProvider>
  )
