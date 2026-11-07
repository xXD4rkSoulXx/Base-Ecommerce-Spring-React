import { useEffect, useState, type SubmitEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { SearchIcon, X } from 'lucide-react'
import { useDebounce } from '@/hooks/useDebounce'

export const AdminCustomerFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const q = searchParams.get('q') || ''

  const [inputValue, setInputValue] = useState(q)
  const debouncedQuery = useDebounce(inputValue.trim(), 400)

  const changeQuery = (value: string) => {
    const next = new URLSearchParams(searchParams)

    if (value) {
      next.set('q', value)
    } else {
      next.delete('q')
    }

    next.delete('page')
    setSearchParams(next)
  }

  useEffect(() => {
    if (debouncedQuery !== q) {
      changeQuery(debouncedQuery)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedQuery])

  const submitSearch = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()
    changeQuery(inputValue.trim())
  }

  const clearQuery = () => {
    setInputValue('')
    changeQuery('')
  }

  return (
    <div className="mb-6 space-y-3 border border-border bg-card p-4">
      <form
        onSubmit={submitSearch}
        className="flex items-center gap-2 border border-border bg-background px-3"
      >
        <SearchIcon className="h-3.5 w-3.5 text-neon-cyan" />
        <input
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Search by name or email…"
          className="w-full bg-transparent py-2.5 text-sm outline-none placeholder:text-muted-foreground"
        />
      </form>
      {q && (
        <div className="flex justify-end">
          <button
            onClick={clearQuery}
            className="inline-flex items-center gap-1 font-mono text-[10px] uppercase tracking-widest text-neon-pink hover:opacity-80"
          >
            <X className="h-3 w-3" /> Clear
          </button>
        </div>
      )}
    </div>
  )
}
