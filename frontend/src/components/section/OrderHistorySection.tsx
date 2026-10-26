import { useEffect, useState, type SubmitEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { SearchIcon, X } from 'lucide-react'
import { OrderDetailDialog } from '@/components/dialog/OrderDetailDialog'
import { OrderStatusBadge } from '@/components/ui/OrderStatusBadge'
import { Pagination } from '@/components/ui/Pagination'
import { useDebounce } from '@/hooks/useDebounce'
import { useOrders } from '@/hooks/useOrders'

const priceFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const formatPrice = (amount: number) => priceFormatter.format(amount)
const dateFormatter = new Intl.DateTimeFormat('pt-PT')
const formatDate = (date: string) => dateFormatter.format(new Date(date))

export const OrderHistorySection = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null)

  const q = searchParams.get('q') || ''
  const from = searchParams.get('from') || ''
  const to = searchParams.get('to') || ''
  const urlPage = Number(searchParams.get('page')) || 1
  const currentPage = urlPage < 1 ? 1 : urlPage
  const hasFilters = Boolean(q || from || to)
  
  const [inputValue, setInputValue] = useState(q)
  const debouncedQuery = useDebounce(inputValue.trim(), 400)

  const { data, isLoading, isError } = useOrders()

  const orders = data?.content || []
  const totalPages = data?.totalPages || 0
  const totalElements = data?.totalElements || 0

  useEffect(() => {
    if (totalPages > 0 && urlPage > totalPages) {
      searchParams.set('page', totalPages.toString())
      setSearchParams(searchParams, { replace: true })
    }
  }, [urlPage, totalPages, searchParams, setSearchParams])

  const handleChangeFilters = (changes: Record<string, string>) => {
    Object.entries(changes).forEach(([key, value]) => {
      if (value) {
        searchParams.set(key, value)
      } else {
        searchParams.delete(key)
      }
    })

    searchParams.delete('page')
    setSearchParams(searchParams)
  }

  useEffect(() => {
    if (debouncedQuery !== q) {
      handleChangeFilters({ q: debouncedQuery })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedQuery])

  const handleChangePage = (newPage: number) => {
    if (newPage >= 1 && newPage <= totalPages) {
      searchParams.set('page', newPage.toString())
      setSearchParams(searchParams)
    }
  }

  const handleSubmitSearch = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()
    handleChangeFilters({ q: inputValue.trim() })
  }

  const handleClearFilters = () => {
    setInputValue('')
    handleChangeFilters({ q: '', from: '', to: '' })
  }

  return (
    <div>
      <h2 className="mb-6 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
        Order History
      </h2>
      <div className="mb-6 space-y-3 border border-border bg-card p-4">
        <form
          onSubmit={handleSubmitSearch}
          className="flex items-center gap-2 border border-border bg-background px-3"
        >
          <SearchIcon className="h-3.5 w-3.5 text-neon-cyan" />
          <input
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder="Search by order # or product…"
            className="w-full bg-transparent py-2.5 text-sm outline-none placeholder:text-muted-foreground"
          />
        </form>
        <div className="grid gap-3 sm:grid-cols-2">
          <label className="block">
            <span className="font-mono text-[9px] uppercase tracking-widest text-muted-foreground">
              From
            </span>
            <input
              type="date"
              value={from}
              max={to || undefined}
              onChange={(e) => handleChangeFilters({ from: e.target.value })}
              className="mt-1 w-full border border-border bg-background px-3 py-2 font-mono text-xs outline-none focus:border-neon-cyan"
            />
          </label>
          <label className="block">
            <span className="font-mono text-[9px] uppercase tracking-widest text-muted-foreground">
              To
            </span>
            <input
              type="date"
              value={to}
              min={from || undefined}
              onChange={(e) => handleChangeFilters({ to: e.target.value })}
              className="mt-1 w-full border border-border bg-background px-3 py-2 font-mono text-xs outline-none focus:border-neon-cyan"
            />
          </label>
        </div>
        <div className="flex items-center justify-between">
          <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            {data && `${totalElements} order${totalElements !== 1 ? 's' : ''}`}
          </span>
          {hasFilters && (
            <button
              onClick={handleClearFilters}
              className="inline-flex items-center gap-1 font-mono text-[10px] uppercase tracking-widest text-neon-pink hover:opacity-80"
            >
              <X className="h-3 w-3" /> Clear
            </button>
          )}
        </div>
      </div>
      <div className="border-t border-border">
        {isLoading ? (
          <p className="border-b border-border py-10 text-center font-mono text-xs text-muted-foreground">
            Loading records...
          </p>
        ) : isError ? (
          <p className="border-b border-border py-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-neon-pink">
            Failed to load orders
          </p>
        ) : orders.length === 0 ? (
          <div className="border-b border-border py-10 text-center">
            <p className="font-mono text-[10px] uppercase tracking-[0.3em] text-neon-pink">
              No transit records
            </p>
            <p className="mt-2 text-sm text-muted-foreground">
              {hasFilters ? 'No orders match the applied filters.' : "You haven't placed any orders yet."}
            </p>
          </div>
        ) : (
          orders.map((order) => (
            <div
              key={order.id}
              className="grid grid-cols-2 items-center gap-x-4 gap-y-2 border-b border-border py-5 sm:grid-cols-[1fr_9rem_7rem_5rem]"
            >
              <div>
                <p className="font-mono text-xs text-neon-cyan">#{order.id}</p>
                <p className="mt-1 text-sm">{formatDate(order.createdAt)}</p>
              </div>
              <OrderStatusBadge status={order.status} />
              <span className="font-mono text-lg text-neon-cyan sm:text-right">
                {formatPrice(order.totalAmount)}
              </span>
              <button
                onClick={() => setSelectedOrderId(order.id)}
                className="text-right text-xs font-bold uppercase tracking-widest hover:text-neon-cyan"
              >
                Detail →
              </button>
            </div>
          ))
        )}
      </div>
      <Pagination currentPage={currentPage} totalPages={totalPages} onChange={handleChangePage} />
      <OrderDetailDialog orderId={selectedOrderId} onClose={() => setSelectedOrderId(null)} />
    </div>
  )
}
