import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ReturnDialog } from '@/components/dialog/ReturnDialog'
import { renderWithProviders } from '@/test/render'
import { apiUrl, server } from '@/test/server'
import type { OrderDetailsResponse, OrderItemResponse } from '@/types/order'

const item = (id: number, productName: string, returnableQuantity: number): OrderItemResponse => ({
  id,
  ref: `REF-${id}`,
  productName,
  color: 'BLACK',
  size: 'M',
  quantity: 2,
  unitPrice: 10,
  lineTotal: 20,
  returnableQuantity
})

const order = {
  id: 5,
  items: [item(11, 'Rain Jacket', 2), item(12, 'Wool Hat', 1), item(13, 'Scarf', 0)]
} as OrderDetailsResponse

let requestBody: unknown

const renderDialog = () => {
  const onClose = vi.fn()

  renderWithProviders(<ReturnDialog order={order} onClose={onClose} />)

  return onClose
}

const increase = (index: number) => screen.getAllByRole('button', { name: 'Increase quantity' })[index]
const decrease = (index: number) => screen.getAllByRole('button', { name: 'Decrease quantity' })[index]
const submit = () => userEvent.click(screen.getByRole('button', { name: 'Request return' }))

describe('ReturnDialog', () => {
  beforeEach(() => {
    requestBody = undefined
    HTMLDialogElement.prototype.showModal = function (this: HTMLDialogElement) {
      this.open = true
    }
    server.use(
      http.post(apiUrl('/api/orders/5/returns'), async ({ request }) => {
        requestBody = await request.json()

        return HttpResponse.json({})
      })
    )
  })

  it('only lists the items that can still be returned', () => {
    renderDialog()

    expect(screen.getByText('Rain Jacket')).toBeInTheDocument()
    expect(screen.getByText('Wool Hat')).toBeInTheDocument()
    expect(screen.queryByText('Scarf')).not.toBeInTheDocument()
  })

  it('keeps each quantity between zero and what can be returned', async () => {
    renderDialog()

    expect(decrease(0)).toBeDisabled()
    expect(increase(1)).toBeEnabled()

    await userEvent.click(increase(1))

    expect(increase(1)).toBeDisabled()
    expect(decrease(1)).toBeEnabled()

    await userEvent.click(increase(0))
    expect(increase(0)).toBeEnabled()

    await userEvent.click(increase(0))
    expect(increase(0)).toBeDisabled()
  })

  it('asks for at least one item and a reason, and sends nothing', async () => {
    renderDialog()

    await submit()

    expect(await screen.findByText('Select at least one item')).toBeInTheDocument()
    expect(screen.getByText('Select a reason', { selector: 'p' })).toBeInTheDocument()
    expect(requestBody).toBeUndefined()
  })

  it('asks for a comment when the reason is Other', async () => {
    renderDialog()
    await userEvent.click(increase(0))
    await userEvent.selectOptions(screen.getByLabelText('Reason'), 'OTHER')

    await submit()

    expect(await screen.findByText('Please describe the reason')).toBeInTheDocument()
    expect(requestBody).toBeUndefined()
  })

  it('sends only the selected items and leaves out an empty comment', async () => {
    const onClose = renderDialog()
    await userEvent.click(increase(0))
    await userEvent.click(increase(0))
    await userEvent.selectOptions(screen.getByLabelText('Reason'), 'DEFECTIVE')

    await submit()

    await waitFor(() => expect(onClose).toHaveBeenCalled())
    expect(requestBody).toEqual({ reason: 'DEFECTIVE', items: [{ orderItemId: 11, quantity: 2 }] })
  })

  it('sends the comment trimmed', async () => {
    renderDialog()
    await userEvent.click(increase(1))
    await userEvent.selectOptions(screen.getByLabelText('Reason'), 'OTHER')
    await userEvent.type(screen.getByLabelText('Comment'), '  Too small  ')

    await submit()

    await waitFor(() => expect(requestBody).toBeDefined())
    expect(requestBody).toEqual({ reason: 'OTHER', comment: 'Too small', items: [{ orderItemId: 12, quantity: 1 }] })
  })
})
