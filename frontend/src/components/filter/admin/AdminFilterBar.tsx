import type { ReactNode } from 'react'
import { X } from 'lucide-react'
import { SearchField } from '@/components/ui/SearchField'
import { useSearchFilter } from '@/hooks/useSearchFilter'
import { useUrlFilters } from '@/hooks/useUrlFilters'

interface AdminFilterBarProps {
  searchPlaceholder: string
  children?: ReactNode
}

const PAGINATION_PARAMS = ['page', 'size']

export const AdminFilterBar = ({ searchPlaceholder, children }: AdminFilterBarProps) => {
  const { searchParams, changeFilters } = useUrlFilters()
  const { inputValue, setInputValue, submitSearch } = useSearchFilter()
  const filterKeys = [...searchParams.keys()].filter((key) => !PAGINATION_PARAMS.includes(key))

  const clearFilters = () => {
    setInputValue('')
    changeFilters(Object.fromEntries(filterKeys.map((key) => [key, ''])))
  }

  return (
    <div className="mb-6 space-y-3 border border-border bg-card p-4">
      <div className="flex flex-col gap-3 sm:flex-row">
        <SearchField
          value={inputValue}
          placeholder={searchPlaceholder}
          onChange={setInputValue}
          onSubmit={submitSearch}
        />
        {children}
      </div>
      {filterKeys.length > 0 && (
        <div className="flex justify-end">
          <button
            type="button"
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
