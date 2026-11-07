import { useEffect, useState, type SubmitEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { SearchIcon, X } from 'lucide-react'
import { useDebounce } from '@/hooks/useDebounce'

export const AdminDiscountCodeFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const q = searchParams.get('q') || ''
  const active = searchParams.get('active') || ''
  const hasFilters = Boolean(q || active)

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
    changeFilters({ q: '', active: '' })
  }

  return (
    <div className="mb-6 space-y-3 border border-border bg-card p-4">
      <div className="grid gap-3 sm:grid-cols-[1fr_14rem]">
        <form
          onSubmit={submitSearch}
          className="flex items-center gap-2 border border-border bg-background px-3"
        >
          <SearchIcon className="h-3.5 w-3.5 text-neon-cyan" />
          <input
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder="Search by code…"
            className="w-full bg-transparent py-2.5 text-sm outline-none placeholder:text-muted-foreground"
          />
        </form>
        <select
          value={active}
          onChange={(e) => changeFilters({ active: e.target.value })}
          className="border border-border bg-background px-3 py-2.5 font-mono text-xs uppercase tracking-widest outline-none focus:border-neon-cyan"
        >
          <option value="">All statuses</option>
          <option value="true">Active</option>
          <option value="false">Inactive</option>
        </select>
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
