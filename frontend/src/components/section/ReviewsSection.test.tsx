import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest'
import { ReviewsSection } from '@/components/section/ReviewsSection'
import { useAuthStore } from '@/store/useAuthStore'
import { buildProductDetails, buildUser } from '@/test/builders'
import { renderWithProviders } from '@/test/render'
import { apiUrl, server } from '@/test/server'

class FakeIntersectionObserver {
  observe = vi.fn()
  disconnect = vi.fn()
}

const initialState = useAuthStore.getInitialState()
const product = buildProductDetails({ id: 1, totalReviews: 2, averageRating: 4.5 })

const review = (id: number, title: string) => ({
  id,
  title,
  description: `Description ${id}`,
  rating: 5,
  userFullName: 'Ana Costa',
  createdAt: '2026-03-01T10:00:00Z'
})

const mockReviews = (content: ReturnType<typeof review>[]) =>
  server.use(
    http.get(apiUrl('/api/products/1/reviews'), () =>
      HttpResponse.json({ content, page: 0, size: 5, totalElements: content.length, totalPages: 1, last: true })
    )
  )

const mockEligibility = (status: string) =>
  server.use(http.get(apiUrl('/api/products/1/review/eligibility'), () => HttpResponse.json({ status })))

const signIn = () => useAuthStore.getState().setAuth(buildUser(), 'token')

describe('ReviewsSection', () => {
  beforeAll(async () => {
    await import('@/components/form/ReviewForm')
  })

  beforeEach(() => {
    vi.stubGlobal('IntersectionObserver', FakeIntersectionObserver)
    useAuthStore.setState(initialState, true)
    localStorage.clear()
    mockReviews([])
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows the total of reviews and the average score', async () => {
    renderWithProviders(<ReviewsSection product={product} />)

    expect(screen.getByRole('heading', { level: 2 })).toHaveTextContent('Reviews (2)')
    expect(screen.getByText('4.5')).toBeInTheDocument()
    await screen.findByText('No signals yet. Be first.')
  })

  it('shows the reviews and the end marker', async () => {
    mockReviews([review(1, 'Great jacket'), review(2, 'Keeps the rain out')])

    renderWithProviders(<ReviewsSection product={product} />)

    expect(await screen.findByText('Great jacket')).toBeInTheDocument()
    expect(screen.getByText('Keeps the rain out')).toBeInTheDocument()
    expect(screen.getByText('— End of transmissions —')).toBeInTheDocument()
    expect(screen.queryByText('No signals yet. Be first.')).not.toBeInTheDocument()
  })

  it('shows the empty state when there are no reviews', async () => {
    renderWithProviders(<ReviewsSection product={product} />)

    expect(await screen.findByText('No signals yet. Be first.')).toBeInTheDocument()
    expect(screen.queryByText('— End of transmissions —')).not.toBeInTheDocument()
  })

  it('tells a visitor that only logged in buyers can review, without asking for eligibility', async () => {
    renderWithProviders(<ReviewsSection product={product} />)

    expect(await screen.findByText(/only logged in buyers/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /write a review/i })).not.toBeInTheDocument()
  })

  it.each([
    ['NOT_PURCHASED', /need to have purchased and received/i],
    ['ALREADY_REVIEWED', /already reviewed this product/i]
  ])('explains why a %s user cannot review', async (status, message) => {
    signIn()
    mockEligibility(status)

    renderWithProviders(<ReviewsSection product={product} />)

    expect(await screen.findByText(message)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /write a review/i })).not.toBeInTheDocument()
  })

  it('lets an eligible user open and cancel the review form', async () => {
    signIn()
    mockEligibility('ELIGIBLE')
    renderWithProviders(<ReviewsSection product={product} />)

    await userEvent.click(await screen.findByRole('button', { name: /write a review/i }))

    expect(await screen.findByLabelText('Title')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /write a review/i })).not.toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }))

    expect(screen.queryByLabelText('Title')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: /write a review/i })).toBeInTheDocument()
  })
})
