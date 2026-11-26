import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Modal } from '@/components/ui/Modal'

const showModal = vi.fn(function (this: HTMLDialogElement) {
  this.open = true
})

const renderModal = (props: Partial<Parameters<typeof Modal>[0]> = {}) => {
  const onClose = vi.fn()

  render(
    <Modal eyebrow="Danger zone" title="Delete account" onClose={onClose} {...props}>
      <p>Modal content</p>
    </Modal>
  )

  return onClose
}

describe('Modal', () => {
  beforeEach(() => {
    HTMLDialogElement.prototype.showModal = showModal
  })

  it('opens itself as a native modal dialog', () => {
    renderModal()

    expect(showModal).toHaveBeenCalledTimes(1)
  })

  it('shows the eyebrow, the content and a dialog named by its title', () => {
    renderModal()

    expect(screen.getByRole('dialog', { name: 'Delete account' })).toBeInTheDocument()
    expect(screen.getByText('Danger zone')).toBeInTheDocument()
    expect(screen.getByText('Modal content')).toBeInTheDocument()
  })

  it('shows the description only when it is given', () => {
    renderModal({ description: 'This cannot be undone' })

    expect(screen.getByText('This cannot be undone')).toBeInTheDocument()
  })

  it('has no description by default', () => {
    renderModal()

    expect(screen.queryByText('This cannot be undone')).not.toBeInTheDocument()
  })

  it('closes with the close button', async () => {
    const onClose = renderModal()

    await userEvent.click(screen.getByRole('button', { name: 'Close' }))

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('closes when the backdrop is clicked', async () => {
    const onClose = renderModal()

    await userEvent.click(screen.getByRole('dialog'))

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('does not close when the content is clicked', async () => {
    const onClose = renderModal()

    await userEvent.click(screen.getByText('Modal content'))

    expect(onClose).not.toHaveBeenCalled()
  })

  it('closes when the browser closes the dialog, for example with Escape', () => {
    const onClose = renderModal()

    fireEvent(screen.getByRole('dialog'), new Event('close'))

    expect(onClose).toHaveBeenCalledTimes(1)
  })
})
