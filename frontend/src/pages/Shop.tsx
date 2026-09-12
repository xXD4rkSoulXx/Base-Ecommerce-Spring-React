import { useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useInfiniteProducts } from '@/hooks/useInfiniteProducts'
import { ProductCard } from '@/components/card/ProductCard'
import { Loading } from '@/pages/Loading'
import { ErrorPage } from '@/pages/ErrorPage'
import type { ProductCategory } from '@/types/product'

const categories: (ProductCategory | 'ALL')[] = [
  'ALL', 'OUTERWEAR', 'TOPS', 'BOTTOMS', 'FOOTWEAR', 'UNDERWEAR', 'ACCESSORIES'
]

export const Shop = () => {
  const [params, setParams] = useSearchParams()

  const currentCategory = (params.get('category') || 'ALL') as ProductCategory | 'ALL'
  const currentSort = params.get('sort') || 'recent'

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError
  } = useInfiniteProducts()

  const allProducts = data?.pages.flatMap((page) => page.content) || []
  
  const observerTarget = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    const target = observerTarget.current

    if (!target) {
      return
    }

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage()
        }
      },
      { threshold: 0.1 }
    )

    observer.observe(target)

    return () => { observer.disconnect() }
  }, [hasNextPage, isFetchingNextPage, fetchNextPage])

  const handleChangeSort = (sortValue: string) => {
    params.set('sort', sortValue)
    setParams(params)
  }

  const handleChangeCategory = (category: ProductCategory | 'ALL') => {
    if (category === 'ALL') {
      params.delete('category')
    } else {
      params.set('category', category)
    }

    setParams(params)
  }

  if (isLoading) {
    return <Loading />
  }

  if (isError) {
    return <ErrorPage />
  }

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-12 md:px-10 md:py-16">
      <header className="mb-12 flex flex-col gap-6 border-b border-border pb-8 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="mt-3 font-display text-5xl uppercase tracking-tighter md:text-7xl">
            All Objects
          </h1>
          <p className="mt-2 font-mono text-xs text-muted-foreground">
            {allProducts.length} item{allProducts.length !== 1 && 's'} loaded from archive
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Sort</span>
          <select
            value={currentSort}
            onChange={(e) => handleChangeSort(e.target.value)}
            className="border border-border bg-background px-3 py-2 text-xs font-bold uppercase tracking-widest focus:border-neon-cyan focus:outline-none cursor-pointer"
          >
            <option value="recent">Latest</option>
            <option value="price_asc">Price ↑</option>
            <option value="price_desc">Price ↓</option>
            <option value="discount">On Sale</option>
            <option value="best_sellers">Most Wanted</option>
            <option value="top_rated">Top Rated</option>
          </select>
        </div>
      </header>
      <div className="grid gap-12 lg:grid-cols-[200px_1fr]">
        <aside>
          <h2 className="mb-4 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
            Categories
          </h2>
          <ul className="space-y-2">
            {categories.map((category) => (
              <li key={category}>
                <button
                  onClick={() => handleChangeCategory(category)}
                  className={`block w-full text-left text-sm font-bold uppercase tracking-tight transition-colors ${
                    currentCategory === category ? "text-neon-cyan" : "hover:text-neon-cyan"
                  }`}
                >
                  {category}
                  {currentCategory === category && <span className="ml-2 text-neon-pink">●</span>}
                </button>
              </li>
            ))}
          </ul>
        </aside>
        <div>
          {allProducts.length > 0 && (
            <div className="grid grid-cols-2 gap-x-4 gap-y-12 pb-12 md:grid-cols-3 lg:gap-x-6">
              {allProducts.map((product, index) => (
                <ProductCard key={`${product.id}-${index}`} product={product} index={index} />
              ))}
            </div>
          )}
          <div ref={observerTarget} className="mt-6 flex h-20 items-center justify-center font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            {isFetchingNextPage && (
              <span className="animate-pulse">FETCHING ADDITIONAL OBJECTS...</span>
            )}
            {!hasNextPage && allProducts.length > 0 && (
              <span>END OF ARCHIVE</span>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
