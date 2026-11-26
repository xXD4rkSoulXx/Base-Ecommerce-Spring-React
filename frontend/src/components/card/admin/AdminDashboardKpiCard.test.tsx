import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { AdminDashboardKpiCard } from '@/components/card/admin/AdminDashboardKpiCard'
import { formatPercentChange } from '@/utils/format'

const renderCard = (props: Partial<Parameters<typeof AdminDashboardKpiCard>[0]> = {}) =>
  render(
    <AdminDashboardKpiCard label="Revenue" value={120} previous={100} format={(value) => `${value} units`} {...props} />
  )

const delta = () => screen.getByText(/previous (period|data)/)

describe('AdminDashboardKpiCard', () => {
  it('shows the label and the formatted value', () => {
    renderCard()

    expect(screen.getByText('Revenue')).toBeInTheDocument()
    expect(screen.getByText('120 units')).toBeInTheDocument()
  })

  it('shows a dash while the value is not loaded', () => {
    renderCard({ value: undefined })

    expect(screen.getByText('—')).toBeInTheDocument()
  })

  it('shows the change against the previous period', () => {
    renderCard({ value: 120, previous: 100 })

    expect(delta()).toHaveTextContent(`${formatPercentChange(20)}% vs previous period`)
  })

  it.each([
    ['there is no previous value', undefined],
    ['the previous value is zero', 0]
  ])('has no change when %s', (_, previous) => {
    renderCard({ previous })

    expect(delta()).toHaveTextContent('No previous data')
    expect(delta()).toHaveClass('text-muted-foreground')
  })

  it('highlights an increase in the good colour', () => {
    renderCard({ value: 120, previous: 100 })

    expect(delta()).toHaveClass('text-neon-cyan')
  })

  it('highlights a decrease in the bad colour', () => {
    renderCard({ value: 80, previous: 100 })

    expect(delta()).toHaveClass('text-neon-pink')
  })

  it('inverts the colours when lower is better', () => {
    renderCard({ value: 80, previous: 100, higherIsBetter: false })
    expect(delta()).toHaveClass('text-neon-cyan')
  })

  it('inverts the colour of an increase when lower is better', () => {
    renderCard({ value: 120, previous: 100, higherIsBetter: false })

    expect(delta()).toHaveClass('text-neon-pink')
  })

  it('is neutral when nothing changed', () => {
    renderCard({ value: 100, previous: 100 })

    expect(delta()).toHaveClass('text-muted-foreground')
  })
})
