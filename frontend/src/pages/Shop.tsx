import { ProductCard } from '@/components/card/ProductCard'
import { FormError } from '@/components/ui/FormError'
import { ALL_FILTER, PRODUCT_CATEGORIES } from '@/constants'
import { useInfiniteProducts } from '@/hooks/useInfiniteProducts'
import { useInfiniteScroll } from '@/hooks/useInfiniteScroll'
import { useShopParams } from '@/hooks/useShopParams'
import { useUrlFilters } from '@/hooks/useUrlFilters'
import type { ProductSort } from '@/types/product'
import { pluralize } from '@/utils/format'

const PRODUCT_SORT_LABELS: Record<ProductSort, string> = {
  recent: 'Latest',
  price_asc: 'Price ↑',
  price_desc: 'Price ↓',
  discount: 'On Sale',
  best_sellers: 'Most Wanted',
  top_rated: 'Top Rated'
}

const CATEGORIES = [ALL_FILTER, ...PRODUCT_CATEGORIES]
const SORT_OPTIONS = Object.entries(PRODUCT_SORT_LABELS) as [ProductSort, string][]

export const Shop = () => {
  const { category: currentCategory, sortBy } = useShopParams()
  const { changeFilters } = useUrlFilters()
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading, isError } = useInfiniteProducts()
  const observerTarget = useInfiniteScroll({ hasNextPage, isFetchingNextPage, fetchNextPage })

  const products = data?.pages.flatMap((page) => page.content) ?? []

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-12 md:px-10 md:py-16">
      <header className="mb-12 flex flex-col gap-6 border-b border-border pb-8 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="mt-3 font-display text-5xl uppercase tracking-tighter md:text-7xl">All Objects</h1>
          <p className="mt-2 font-mono text-xs text-muted-foreground">
            {pluralize(products.length, 'item')} loaded from archive
          </p>
        </div>
        <label className="flex items-center gap-3">
          <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Sort</span>
          <select
            value={sortBy}
            onChange={(event) => changeFilters({ sort: event.target.value })}
            className="cursor-pointer border border-border bg-background px-3 py-2 text-xs font-bold uppercase tracking-widest focus:border-neon-cyan focus:outline-none"
          >
            {SORT_OPTIONS.map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>
      </header>
      <div className="grid gap-12 lg:grid-cols-[200px_1fr]">
        <aside>
          <h2 className="mb-4 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Categories</h2>
          <ul className="space-y-2">
            {CATEGORIES.map((category) => (
              <li key={category}>
                <button
                  type="button"
                  onClick={() => changeFilters({ category: category === ALL_FILTER ? '' : category })}
                  aria-pressed={currentCategory === category}
                  className={`block w-full text-left text-sm font-bold uppercase tracking-tight transition-colors ${
                    currentCategory === category ? 'text-neon-cyan' : 'hover:text-neon-cyan'
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
          {isLoading && <p className="font-mono text-xs text-muted-foreground">Loading archive...</p>}
          {isError && <FormError message="Could not load the products" />}
          {products.length > 0 && (
            <div className="grid grid-cols-2 gap-x-4 gap-y-12 pb-12 md:grid-cols-3 lg:gap-x-6">
              {products.map((product, index) => (
                <ProductCard key={product.id} product={product} index={index} />
              ))}
            </div>
          )}
          <div
            ref={observerTarget}
            className="mt-6 flex h-20 items-center justify-center font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground"
          >
            {isFetchingNextPage && <span className="animate-pulse">FETCHING ADDITIONAL OBJECTS...</span>}
            {!hasNextPage && products.length > 0 && <span>END OF ARCHIVE</span>}
          </div>
        </div>
      </div>
    </div>
  )
}
