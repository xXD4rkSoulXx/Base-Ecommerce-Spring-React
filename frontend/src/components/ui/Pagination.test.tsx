import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Pagination } from '@/components/ui/Pagination'

describe('Pagination', () => {
  it.each([0, 1])('renders nothing when there are %s pages', (totalPages) => {
    render(<Pagination currentPage={1} totalPages={totalPages} onChange={vi.fn()} />)

    expect(screen.queryByRole('navigation')).not.toBeInTheDocument()
  })

  it('shows the current page and the total', () => {
    render(<Pagination currentPage={2} totalPages={5} onChange={vi.fn()} />)

    expect(screen.getByRole('navigation')).toHaveTextContent('Page 2 of 5')
  })

  it('disables the previous button on the first page', () => {
    render(<Pagination currentPage={1} totalPages={5} onChange={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Previous page' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Next page' })).toBeEnabled()
  })

  it('disables the next button on the last page', () => {
    render(<Pagination currentPage={5} totalPages={5} onChange={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Previous page' })).toBeEnabled()
    expect(screen.getByRole('button', { name: 'Next page' })).toBeDisabled()
  })

  it('asks for the previous and the next page', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()

    render(<Pagination currentPage={3} totalPages={5} onChange={onChange} />)

    await user.click(screen.getByRole('button', { name: 'Previous page' }))
    await user.click(screen.getByRole('button', { name: 'Next page' }))

    expect(onChange).toHaveBeenNthCalledWith(1, 2)
    expect(onChange).toHaveBeenNthCalledWith(2, 4)
  })
})
