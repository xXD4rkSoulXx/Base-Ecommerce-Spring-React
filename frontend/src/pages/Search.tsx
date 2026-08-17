import { useEffect, useState } from 'react'
import type { SubmitEvent } from 'react' 
import { useSearchParams } from 'react-router-dom'
import { SearchIcon } from 'lucide-react'
import { useSearchProducts } from '@/hooks/useSearchProducts'
import { ProductCard } from '@/components/card/ProductCard'
import { Loading } from '@/pages//Loading'
import { ErrorPage } from '@/pages/ErrorPage'

export const Search = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const queryFromUrl = searchParams.get('q') || ''
  const urlPage = searchParams.get('page') !== null ? Number(searchParams.get('page')) : 1
  const [inputValue, setInputValue] = useState(queryFromUrl)
  const { data, isLoading, isError } = useSearchProducts()
  const products = data?.content || []
  const totalPages = data?.totalPages || 0
  const totalElements = data?.totalElements || 0
  let currentPage = urlPage < 1 ? 1 : urlPage

  if (totalPages > 0 && currentPage > totalPages) {
    currentPage = totalPages
  }

  useEffect(() => {
    if (totalPages > 0) {
      if (urlPage < 1) {
        searchParams.set('page', '1')
        setSearchParams(searchParams, { replace: true })
      } else if (urlPage > totalPages) {
        searchParams.set('page', totalPages.toString())
        setSearchParams(searchParams, { replace: true })
      }
    }
  }, [urlPage, totalPages, searchParams, setSearchParams])

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }, [currentPage])

  const handleSearchSubmit = (searchTerm: string) => {
    if (!searchTerm.trim()) {
      setSearchParams({})
    } else {
      setSearchParams({ q: searchTerm, page: '1' })
    }
  }

  const handlePageChange = (newPage: number) => {
    if (newPage >= 1 && newPage <= totalPages) {
      searchParams.set('page', newPage.toString())
      setSearchParams(searchParams)
    }
  }

  const handleSubmit = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()
    handleSearchSubmit(inputValue)
  }

  const handleClear = () => {
    setInputValue('')
    handleSearchSubmit('')
  }

  if (isLoading) {
    return <Loading />
  }

  if (isError) {
    return <ErrorPage />
  }

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
      <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-7xl">
        Locate
      </h1>
      <form 
        onSubmit={handleSubmit}
        className="mt-10 flex items-center gap-3 border-b-2 border-foreground pb-3"
      >
        <SearchIcon className="h-5 w-5 text-neon-cyan" />
        <input
          key={queryFromUrl}
          autoFocus
          defaultValue={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Search by name, REF, or category… (Press Enter)"
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
      {queryFromUrl && (
        <div className="mt-12">
          <p className="mb-8 font-mono text-xs text-muted-foreground">
            {totalElements} result{totalElements !== 1 && 's'} for "{queryFromUrl}"
          </p>
          {products.length > 0 && (
            <div className="grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-4 lg:gap-x-6">
              {products.map((p, i) => (
                <ProductCard key={p.id} product={p} index={i} />
              ))}
            </div>
          )}
          {totalPages > 1 && (
            <div className="mt-16 flex items-center justify-center gap-4 border-t border-border pt-8 font-mono text-xs">
              <button
                disabled={currentPage === 1}
                onClick={() => handlePageChange(currentPage - 1)}
                className="border border-border px-4 py-2 uppercase tracking-widest transition-colors hover:border-neon-cyan hover:text-neon-cyan disabled:opacity-30 disabled:hover:border-border disabled:hover:text-muted-foreground"
              >
                PREV
              </button>   
              <span className="text-muted-foreground uppercase tracking-wider">
                Page <span className="font-bold text-foreground">{currentPage}</span> of <span className="font-bold text-foreground">{totalPages}</span>
              </span>
              <button
                disabled={currentPage === totalPages}
                onClick={() => handlePageChange(currentPage + 1)}
                className="border border-border px-4 py-2 uppercase tracking-widest transition-colors hover:border-neon-cyan hover:text-neon-cyan disabled:opacity-30 disabled:hover:border-border disabled:hover:text-muted-foreground"
              >
                NEXT
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
