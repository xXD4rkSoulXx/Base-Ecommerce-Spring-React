import { useEffect, useState, type SubmitEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { SearchIcon, X } from 'lucide-react'
import { useDebounce } from '@/hooks/useDebounce'

export const AdminStockFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const lowStock = searchParams.get('lowStock') === 'true'
  const hasFilters = Boolean(q || lowStock)

  const [inputValue, setInputValue] = useState(q)
  const debouncedQuery = useDebounce(inputValue.trim(), 400)

  const changeFilters = (changes: Record<string, string>) => {
    const next = new URLSearchParams(searchParams)

    Object.entries(changes).forEach(([key, value]) => {
      if (value) {
        next.set(key, value)
      } else {
        next.delete(key)
      }
    })

    next.delete('page')
    setSearchParams(next)
  }

  useEffect(() => {
    if (debouncedQuery !== q) {
      changeFilters({ q: debouncedQuery })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedQuery])

  const submitSearch = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()
    changeFilters({ q: inputValue.trim() })
  }

  const clearFilters = () => {
    setInputValue('')
    changeFilters({ q: '', lowStock: '' })
  }

  return (
    <div className="mb-6 space-y-3 border border-border bg-card p-4">
      <div className="grid gap-3 sm:grid-cols-[1fr_auto]">
        <form
          onSubmit={submitSearch}
          className="flex items-center gap-2 border border-border bg-background px-3"
        >
          <SearchIcon className="h-3.5 w-3.5 text-neon-cyan" />
          <input
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder="Search by product name or ref…"
            className="w-full bg-transparent py-2.5 text-sm outline-none placeholder:text-muted-foreground"
          />
        </form>
        <label className="flex cursor-pointer items-center gap-2 border border-border bg-background px-3 py-2.5 font-mono text-xs uppercase tracking-widest">
          <input
            type="checkbox"
            checked={lowStock}
            onChange={(e) => changeFilters({ lowStock: e.target.checked ? 'true' : '' })}
            className="accent-neon-cyan"
          />
          Low stock
        </label>
      </div>
      {hasFilters && (
        <div className="flex justify-end">
          <button
            onClick={clearFilters}
            className="inline-flex items-center gap-1 font-mono text-[10px] uppercase tracking-widest text-neon-pink hover:opacity-80"
          >
            <X className="h-3 w-3" /> Clear
          </button>
        </div>
      )}
    </div>
  )
}
