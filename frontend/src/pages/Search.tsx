import { useEffect, useState } from 'react'
import type { SubmitEvent } from 'react' 
import { useSearchParams } from 'react-router-dom'
import { SearchIcon } from 'lucide-react'
import { Loading } from '@/pages/Loading'
import { ErrorPage } from '@/pages/ErrorPage'
import { Pagination } from '@/components/ui/Pagination'
import { ProductCard } from '@/components/card/ProductCard'
import { useDebounce } from '@/hooks/useDebounce'
import { useSearchProducts } from '@/hooks/useSearchProducts'

export const Search = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const queryFromUrl = searchParams.get('q') || ''
  const urlPage = searchParams.get('page') !== null ? Number(searchParams.get('page')) : 1

  const [inputValue, setInputValue] = useState(queryFromUrl)
  const debouncedInput = useDebounce(inputValue.trim(), 400)

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

  const handleSearch = (searchTerm: string) => {
    if (!searchTerm.trim()) {
      setSearchParams({})
    } else {
      setSearchParams({ q: searchTerm, page: '1' })
    }
  }

  useEffect(() => {
    if (debouncedInput !== queryFromUrl) {
      handleSearch(debouncedInput)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedInput])

  const handleChangePage = (newPage: number) => {
    if (newPage >= 1 && newPage <= totalPages) {
      searchParams.set('page', newPage.toString())
      setSearchParams(searchParams)
    }
  }

  const handleSubmitSearch = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()
    handleSearch(inputValue)
  }

  const handleClearInput = () => {
    setInputValue('')
    handleSearch('')
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
        onSubmit={handleSubmitSearch}
        className="mt-10 flex items-center gap-3 border-b-2 border-foreground pb-3"
      >
        <SearchIcon className="h-5 w-5 text-neon-cyan" />
        <input
          autoFocus
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Search by name or REF…"
          className="w-full bg-transparent text-2xl placeholder:text-muted-foreground focus:outline-none md:text-3xl"
        />
        {inputValue && (
          <button 
            type="button"
            onClick={handleClearInput}
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
          <Pagination currentPage={currentPage} totalPages={totalPages} onChange={handleChangePage} />
        </div>
      )}
    </div>
  )
}
