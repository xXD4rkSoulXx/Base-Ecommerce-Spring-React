import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'
import { AdminReturnCard } from '@/components/card/admin/AdminReturnCard'
import { AdminRejectReturnDialog } from '@/components/dialog/admin/AdminRejectReturnDialog'
import { AdminReturnStatusFilter } from '@/components/filter/admin/AdminReturnStatusFilter'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { TODO_FILTER } from '@/constants'
import { useAdminApproveReturn } from '@/hooks/admin/useAdminApproveReturn'
import { useAdminReceiveReturn } from '@/hooks/admin/useAdminReceiveReturn'
import { useAdminRefundReturn } from '@/hooks/admin/useAdminRefundReturn'
import { useAdminReturns } from '@/hooks/admin/useAdminReturns'
import { usePagination } from '@/hooks/usePagination'
import { usePermission } from '@/hooks/usePermission'
import type { AdminReturnResponse } from '@/types/admin/adminReturn'
import { getErrorMessage } from '@/utils/error'

export const AdminReturns = () => {
  const [searchParams] = useSearchParams()
  const { data, isLoading, isError } = useAdminReturns()
  const { mutate: approveReturn, isPending: isApproving } = useAdminApproveReturn()
  const { mutate: receiveReturn, isPending: isReceiving } = useAdminReceiveReturn()
  const { mutate: refundReturn, isPending: isRefunding } = useAdminRefundReturn()
  const canEdit = usePermission('RETURNS_WRITE')
  const [error, setError] = useState<string | null>(null)
  const [rejecting, setRejecting] = useState<AdminReturnResponse | null>(null)

  const returns = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const { currentPage, changePage } = usePagination(totalPages)
  const isBusy = isApproving || isReceiving || isRefunding
  const isTodo = (searchParams.get('status') || TODO_FILTER) === TODO_FILTER

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
  }

  return (
    <div className="p-6 md:p-10">
      <AdminPageTitle count={data?.totalElements}>Returns</AdminPageTitle>
      <AdminReturnStatusFilter />
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
          {isTodo ? 'Nothing waiting for action' : 'No returns found'}
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
      {rejecting && <AdminRejectReturnDialog orderReturn={rejecting} onClose={() => setRejecting(null)} />}
    </div>
  )
}
