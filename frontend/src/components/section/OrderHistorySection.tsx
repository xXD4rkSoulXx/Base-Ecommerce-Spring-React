import { useState } from 'react'
import { X } from 'lucide-react'
import { OrderDetailDialog } from '@/components/dialog/OrderDetailDialog'
import { OrderRow } from '@/components/row/OrderRow'
import { Pagination } from '@/components/ui/Pagination'
import { SearchField } from '@/components/ui/SearchField'
import { useOrders } from '@/hooks/useOrders'
import { usePagination } from '@/hooks/usePagination'
import { useSearchFilter } from '@/hooks/useSearchFilter'
import { useUrlFilters } from '@/hooks/useUrlFilters'
import { pluralize } from '@/utils/format'

export const OrderHistorySection = () => {
  const { searchParams, changeFilters } = useUrlFilters()
  const { inputValue, setInputValue, submitSearch } = useSearchFilter()
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null)
  const { data, isLoading, isError } = useOrders()

  const q = searchParams.get('q') || ''
  const from = searchParams.get('from') || ''
  const to = searchParams.get('to') || ''
  const hasFilters = Boolean(q || from || to)

  const orders = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const totalElements = data?.totalElements ?? 0
  const { currentPage, changePage } = usePagination(totalPages)

  const clearFilters = () => {
    setInputValue('')
    changeFilters({ q: '', from: '', to: '' })
  }

  return (
    <div>
      <h2 className="mb-6 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Order History</h2>
      <div className="mb-6 space-y-3 border border-border bg-card p-4">
        <SearchField
          value={inputValue}
          placeholder="Search by order # or product…"
          onChange={setInputValue}
          onSubmit={submitSearch}
        />
        <div className="grid gap-3 sm:grid-cols-2">
          <label className="block">
            <span className="font-mono text-[9px] uppercase tracking-widest text-muted-foreground">From</span>
            <input
              type="date"
              value={from}
              max={to || undefined}
              onChange={(event) => changeFilters({ from: event.target.value })}
              className="mt-1 w-full border border-border bg-background px-3 py-2 font-mono text-xs outline-none focus:border-neon-cyan"
            />
          </label>
          <label className="block">
            <span className="font-mono text-[9px] uppercase tracking-widest text-muted-foreground">To</span>
            <input
              type="date"
              value={to}
              min={from || undefined}
              onChange={(event) => changeFilters({ to: event.target.value })}
              className="mt-1 w-full border border-border bg-background px-3 py-2 font-mono text-xs outline-none focus:border-neon-cyan"
            />
          </label>
        </div>
        <div className="flex items-center justify-between">
          <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            {data && pluralize(totalElements, 'order')}
          </span>
          {hasFilters && (
            <button
              type="button"
              onClick={clearFilters}
              className="inline-flex items-center gap-1 font-mono text-[10px] uppercase tracking-widest text-neon-pink hover:opacity-80"
            >
              <X className="h-3 w-3" /> Clear
            </button>
          )}
        </div>
      </div>
      <div className="border-t border-border">
        {isLoading && (
          <p className="border-b border-border py-10 text-center font-mono text-xs text-muted-foreground">
            Loading records...
          </p>
        )}
        {isError && (
          <p className="border-b border-border py-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-neon-pink">
            Failed to load orders
          </p>
        )}
        {!isLoading && !isError && orders.length === 0 && (
          <div className="border-b border-border py-10 text-center">
            <p className="font-mono text-[10px] uppercase tracking-[0.3em] text-neon-pink">No transit records</p>
            <p className="mt-2 text-sm text-muted-foreground">
              {hasFilters ? 'No orders match the applied filters.' : "You haven't placed any orders yet."}
            </p>
          </div>
        )}
        {orders.map((order) => (
          <OrderRow key={order.id} order={order} onSelect={setSelectedOrderId} />
        ))}
      </div>
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={changePage} />
      <OrderDetailDialog orderId={selectedOrderId} onClose={() => setSelectedOrderId(null)} />
    </div>
  )
}
