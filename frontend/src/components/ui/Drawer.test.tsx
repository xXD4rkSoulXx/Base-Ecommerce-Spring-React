import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { Drawer } from '@/components/ui/Drawer'

const renderDrawer = (open: boolean, onClose = vi.fn()) => {
  const ui = (isOpen: boolean) => (
    <Drawer open={isOpen} onClose={onClose} label="Menu" hideFrom="lg">
      <p>Drawer content</p>
    </Drawer>
  )
  const view = render(ui(open))

  return { onClose, rerenderWith: (isOpen: boolean) => view.rerender(ui(isOpen)), unmount: view.unmount }
}

describe('Drawer', () => {
  afterEach(() => {
    document.body.style.overflow = ''
  })

  it('renders its content in a dialog with the given label', () => {
    renderDrawer(true)

    expect(screen.getByRole('dialog', { name: 'Menu' })).toHaveTextContent('Drawer content')
  })

  it('is inert while closed and interactive while open', () => {
    const { rerenderWith } = renderDrawer(false)
    const wrapper = screen.getByRole('dialog', { hidden: true }).parentElement

    expect(wrapper).toHaveAttribute('inert')

    rerenderWith(true)

    expect(wrapper).not.toHaveAttribute('inert')
  })

  it('closes with the close button', async () => {
    const { onClose } = renderDrawer(true)

    await userEvent.click(screen.getByRole('button', { name: 'Close menu' }))

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('closes when the backdrop is clicked', async () => {
    const { onClose } = renderDrawer(true)
    const backdrop = screen.getByRole('dialog').previousElementSibling as HTMLElement

    await userEvent.click(backdrop)

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('closes with Escape only while open', () => {
    const { onClose, rerenderWith } = renderDrawer(false)

    fireEvent.keyDown(document, { key: 'Escape' })
    expect(onClose).not.toHaveBeenCalled()

    rerenderWith(true)
    fireEvent.keyDown(document, { key: 'Escape' })
    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('blocks the page scroll while open and restores it afterwards', () => {
    const { rerenderWith, unmount } = renderDrawer(true)

    expect(document.body.style.overflow).toBe('hidden')

    rerenderWith(false)
    expect(document.body.style.overflow).toBe('')

    rerenderWith(true)
    unmount()
    expect(document.body.style.overflow).toBe('')
  })
})
