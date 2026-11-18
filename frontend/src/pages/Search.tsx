import { useEffect } from 'react'
import { SearchIcon } from 'lucide-react'
import { ProductCard } from '@/components/card/ProductCard'
import { FormError } from '@/components/ui/FormError'
import { Pagination } from '@/components/ui/Pagination'
import { usePagination } from '@/hooks/usePagination'
import { useSearchFilter } from '@/hooks/useSearchFilter'
import { useSearchProducts } from '@/hooks/useSearchProducts'
import { useUrlFilters } from '@/hooks/useUrlFilters'
import { pluralize } from '@/utils/format'

export const Search = () => {
  const { searchParams, changeFilters } = useUrlFilters()
  const { inputValue, setInputValue, submitSearch } = useSearchFilter()
  const { data, isLoading, isError } = useSearchProducts()
  const { currentPage, changePage } = usePagination(data?.totalPages ?? 0)

  const q = searchParams.get('q') || ''

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }, [currentPage])

  const handleClear = () => {
    setInputValue('')
    changeFilters({ q: '' })
  }

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
      <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-7xl">Locate</h1>
      <form onSubmit={submitSearch} className="mt-10 flex items-center gap-3 border-b-2 border-foreground pb-3">
        <SearchIcon aria-hidden="true" className="h-5 w-5 text-neon-cyan" />
        <input
          autoFocus
          value={inputValue}
          onChange={(event) => setInputValue(event.target.value)}
          aria-label="Search products"
          placeholder="Search by name or REF…"
          className="w-full bg-transparent text-2xl placeholder:text-muted-foreground focus:outline-none md:text-3xl"
        />
        {inputValue && (
          <button
            type="button"
            onClick={handleClear}
            className="text-xs uppercase text-muted-foreground hover:text-foreground"
          >
            Clear
          </button>
        )}
      </form>
      {q && (
        <div className="mt-12">
          {isLoading && <p className="font-mono text-xs text-muted-foreground">Searching...</p>}
          {isError && <FormError message="Could not load the results" />}
          {data && (
            <>
              <p className="mb-8 font-mono text-xs text-muted-foreground">
                {pluralize(data.totalElements, 'result')} for "{q}"
              </p>
              {data.content.length > 0 && (
                <div className="grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-4 lg:gap-x-6">
                  {data.content.map((product, index) => (
                    <ProductCard key={product.id} product={product} index={index} />
                  ))}
                </div>
              )}
              <Pagination currentPage={currentPage} totalPages={data.totalPages} onChange={changePage} />
            </>
          )}
        </div>
      )}
    </div>
  )
}
