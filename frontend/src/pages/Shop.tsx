import { useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'

import Error from './Error'
import Loading from './Loading'
import ProductCard from '@/components/card/ProductCard'

import type { ProductCategory } from '@/types/Product'

import { useShopProductsQuery } from '@/hooks/queries/useShopProductsQuery'

function Shop() {
  const [params, setParams] = useSearchParams();
  
  const currentCategory = (params.get("category") || "ALL") as ProductCategory | "ALL";
  const currentSort = params.get("sort") || "recent";

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError
  } = useShopProductsQuery();

  const allProducts = data?.pages.flatMap((page) => page.content) || [];

  const observerTarget = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const target = observerTarget.current;
    if (!target) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage();
        }
      },
      { threshold: 0.1 }
    );

    observer.observe(target);

    return () => { observer.unobserve(target); };
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  const handleCategoryChange = (category: ProductCategory | "ALL") => {
    if (category === "ALL") {
      params.delete("category");
    } else {
      params.set("category", category);
    }
    setParams(params);
  };

  const handleSortChange = (sortValue: string) => {
    params.set("sort", sortValue);
    setParams(params);
  };

  const categoriesList: (ProductCategory | "ALL")[] = [
    "ALL", "OUTERWEAR", "TOPS", "BOTTOMS", "FOOTWEAR", "UNDERWEAR", "ACCESSORIES"
  ];

  if (isLoading) {
    return <Loading />;
  }

  if (isError) {
    return <Error />;
  }

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-12 md:px-10 md:py-16">
      <header className="mb-12 flex flex-col gap-6 border-b border-border pb-8 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="mt-3 font-display text-5xl uppercase tracking-tighter md:text-7xl">
            All Objects
          </h1>
          <p className="mt-2 font-mono text-xs text-muted-foreground">
            {allProducts.length} item{allProducts.length !== 1 && "s"} loaded from archive
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">Sort</span>
          <select
            value={currentSort}
            onChange={(e) => handleSortChange(e.target.value)}
            className="border border-border bg-background px-3 py-2 text-xs font-bold uppercase tracking-widest focus:border-neon-cyan focus:outline-none cursor-pointer"
          >
            <option value="recent">Latest</option>
            <option value="price_asc">Price ↑</option>
            <option value="price_desc">Price ↓</option>
          </select>
        </div>
      </header>
      <div className="grid gap-12 lg:grid-cols-[200px_1fr]">
        <aside>
          <h2 className="mb-4 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
            Categories
          </h2>
          <ul className="space-y-2">
            {categoriesList.map((c) => (
              <li key={c}>
                <button
                  onClick={() => handleCategoryChange(c)}
                  className={`block w-full text-left text-sm font-bold uppercase tracking-tight transition-colors ${
                    currentCategory === c ? "text-neon-cyan" : "hover:text-neon-cyan"
                  }`}
                >
                  {c}
                  {currentCategory === c && <span className="ml-2 text-neon-pink">●</span>}
                </button>
              </li>
            ))}
          </ul>
        </aside>
        <div>
          {allProducts.length > 0 && (
            <div className="grid grid-cols-2 gap-x-4 gap-y-12 pb-12 md:grid-cols-3 lg:gap-x-6">
              {allProducts.map((product, index) => (
                <ProductCard key={product.id} product={product} index={index} />
              ))}
            </div>
          )}
          <div ref={observerTarget} className="mt-6 flex h-20 items-center justify-center font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            {isFetchingNextPage ? (
              <span className="animate-pulse">FETCHING ADDITIONAL OBJECTS...</span>
            ) : hasNextPage ? (
              <span>SCROLL FOR MORE</span>
            ) : allProducts.length > 0 ? (
              <span>END OF ARCHIVE</span>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Shop
