import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { RejectReturnDialog } from '@/components/dialog/admin/RejectReturnDialog'
import { AdminReturnCard } from '@/components/card/admin/AdminReturnCard'
import { AdminReturnTabs } from '@/components/section/admin/AdminReturnTabs'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { useAdminReturns } from '@/hooks/admin/useAdminReturns'
import { useApproveReturn } from '@/hooks/admin/useApproveReturn'
import { useReceiveReturn } from '@/hooks/admin/useReceiveReturn'
import { useRefundReturn } from '@/hooks/admin/useRefundReturn'
import { usePermission } from '@/hooks/usePermission'
import type { AdminReturnResponse } from '@/types/admin/return'

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminReturns = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const { data, isLoading, isError } = useAdminReturns()
  const { mutate: approveReturn, isPending: isApproving } = useApproveReturn()
  const { mutate: receiveReturn, isPending: isReceiving } = useReceiveReturn()
  const { mutate: refundReturn, isPending: isRefunding } = useRefundReturn()
  const canEdit = usePermission('RETURNS_WRITE')
  const [error, setError] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [rejecting, setRejecting] = useState<AdminReturnResponse | null>(null)

  const urlPage = Number(searchParams.get('page')) || 1
  const currentPage = urlPage < 1 ? 1 : urlPage
  const returns = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const isBusy = isApproving || isReceiving || isRefunding

  useEffect(() => {
    if (totalPages > 0 && urlPage > totalPages) {
      const next = new URLSearchParams(searchParams)

      next.set('page', totalPages.toString())
      setSearchParams(next, { replace: true })
    }
  }, [urlPage, totalPages, searchParams, setSearchParams])

  const changePage = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      const next = new URLSearchParams(searchParams)

      next.set('page', page.toString())
      setSearchParams(next)
    }
  }

  const showError = (fallback: string) => (mutationError: unknown) =>
    setError(getErrorMessage(mutationError, fallback))

  const approve = (id: number) => {
    setError(null)

    approveReturn(id, {
      onSuccess: () => toast.success(`Return #${id} approved`),
      onError: showError('Could not approve the return')
    })
  }

  const receive = (id: number) => {
    setError(null)

    receiveReturn(id, {
      onSuccess: () => toast.success(`Return #${id} marked as received`),
      onError: showError('Could not mark the return as received')
    })
  }

  const refund = (id: number) => {
    setError(null)

    refundReturn(id, {
      onSuccess: (result) => {
        if (result.status === 'REFUNDED') {
          toast.success(`Return #${id} refunded`)
        } else {
          setError('The refund could not be processed. The return is still received, try again.')
        }
      },
      onError: showError('Could not refund the return')
    })
  }

  const openReject = (orderReturn: AdminReturnResponse) => {
    setError(null)
    setRejecting(orderReturn)
    setDialogOpen(true)
  }

  return (
    <div className="p-6 md:p-10">
      <h1 className="mb-8 font-display text-5xl uppercase tracking-tighter md:text-7xl">
        {data ? `Returns (${data.totalElements})` : 'Returns'}
      </h1>
      <AdminReturnTabs />
      {error && (
        <div className="mb-6">
          <FormError message={error} />
        </div>
      )}
      {isError && <FormError message="Could not load the returns" />}
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {data && returns.length === 0 && (
        <p className="border border-border bg-card p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
          {(searchParams.get('status') || ['admin', 'returns']) === 'TODO'
            ? 'Nothing waiting for action'
            : 'No returns found'}
        </p>
      )}
      <div className="space-y-4">
        {returns.map((orderReturn) => (
          <AdminReturnCard
            key={orderReturn.id}
            orderReturn={orderReturn}
            canEdit={canEdit}
            disabled={isBusy}
            onApprove={approve}
            onReject={openReject}
            onReceive={receive}
            onRefund={refund}
          />
        ))}
      </div>
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
      <RejectReturnDialog
        key={rejecting?.id ?? 'none'}
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        orderReturn={rejecting}
      />
    </div>
  )
}
