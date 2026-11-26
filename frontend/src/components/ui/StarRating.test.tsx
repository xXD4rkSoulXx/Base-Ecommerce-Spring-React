import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { StarRating } from '@/components/ui/StarRating'

describe('StarRating', () => {
  describe('interactive', () => {
    it('shows five star buttons', () => {
      render(<StarRating value={0} onChange={vi.fn()} />)

      expect(screen.getByRole('group', { name: 'Rating' })).toBeInTheDocument()
      expect(screen.getAllByRole('button')).toHaveLength(5)
      expect(screen.getByRole('button', { name: '1 star' })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: '5 stars' })).toBeInTheDocument()
    })

    it('reports the star that was clicked', async () => {
      const user = userEvent.setup()
      const onChange = vi.fn()

      render(<StarRating value={0} onChange={onChange} />)
      await user.click(screen.getByRole('button', { name: '4 stars' }))

      expect(onChange).toHaveBeenCalledWith(4)
    })

    it('marks the star that matches the rounded value', () => {
      render(<StarRating value={3.6} onChange={vi.fn()} />)

      expect(screen.getByRole('button', { name: '4 stars' })).toHaveAttribute('aria-pressed', 'true')
      expect(screen.getByRole('button', { name: '3 stars' })).toHaveAttribute('aria-pressed', 'false')
    })
  })

  describe('read only', () => {
    it('shows the rounded rating as an image and has no buttons', () => {
      render(<StarRating value={3.6} readOnly />)

      expect(screen.getByRole('img', { name: 'Rated 4 out of 5' })).toBeInTheDocument()
      expect(screen.queryByRole('button')).not.toBeInTheDocument()
    })
  })
})
