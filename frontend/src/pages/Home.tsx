import { Link } from 'react-router-dom'
import { SectionDivider } from '@/components/ui/SectionDivider'
import { ProductGridSection } from '@/components/card/ProductGridSection'
import { useFeaturedProducts } from '@/hooks/useFeaturedProducts'
import { useDealProducts } from '@/hooks/useDealProducts'
import { useBestSellers } from '@/hooks/useBestSellers'
import { useNewArrivals } from '@/hooks/useNewArrivals'
import { useTopRatedProducts } from '@/hooks/useTopRatedProducts'

export const Home = () => {
  const { data: featured } = useFeaturedProducts()
  const { data: deals } = useDealProducts()
  const { data: bestSellers } = useBestSellers()
  const { data: newArrivals } = useNewArrivals()
  const { data: topRated } = useTopRatedProducts()

  const slotA = featured?.[0]
  const slotB = featured?.[1]

  return (
    <>
      {slotA && (
        <section className="relative overflow-hidden grid-noise">
          <div className="pointer-events-none absolute -left-10 -top-20 select-none opacity-[0.04]">
            <h1 className="font-display text-[28vw] uppercase leading-none tracking-tighter">
              SYSTEM
            </h1>
          </div>
          <div className="relative mx-auto grid max-w-[1440px] grid-cols-12 gap-6 px-6 pb-32 pt-16 md:px-10">
            <div className="col-span-12 flex flex-col justify-end pb-8 lg:col-span-5">
              <h1 className="font-display text-7xl uppercase leading-[0.85] tracking-tighter md:text-8xl lg:text-[clamp(3.5rem,6vw,6.875rem)]">
                Raw <br />
                <span className="italic text-neon-cyan">Urban</span> <br />
                Static
              </h1>
              <p className="mt-8 max-w-md text-base leading-relaxed text-muted-foreground md:text-lg">
                Engineered for the high-velocity transition from daylight to deep neon. Reflective fibers. Concrete-grade textiles.
              </p>
              <div className="mt-10 flex flex-wrap gap-3">
                <Link
                  to="/shop"
                  className="bg-foreground px-8 py-4 text-xs font-black uppercase tracking-widest text-background transition-colors hover:bg-neon-cyan hover:text-primary-foreground md:px-12 md:py-5"
                >
                  Shop Collection
                </Link>
              </div>
            </div>
            <div className={`col-span-12 grid gap-4 lg:gap-6 ${slotB ? 'sm:grid-cols-2 lg:col-span-7' : 'grid-cols-1 lg:col-span-7'}`}>
              <Link to={`/product/${slotA.id}`} className="relative block">
                <div className="aspect-[3/4] overflow-hidden border border-border bg-card">
                  <img
                    src={slotA.cover}
                    alt={slotA.name}
                    className="h-full w-full object-cover"
                  />
                </div>
                <div className="absolute left-3 top-3 bg-background/70 px-2 py-1 font-mono text-[9px] font-bold backdrop-blur">
                  REF: {slotA.ref}
                </div>
              </Link>
              {slotB && (
                <Link to={`/product/${slotB.id}`} className="relative block lg:translate-y-24">
                  <div className="aspect-[3/4] overflow-hidden border border-border bg-card">
                    <img
                      src={slotB.cover}
                      alt={slotB.name}
                      loading="lazy"
                      className="h-full w-full object-cover grayscale-[30%]"
                    />
                  </div>
                  <div className="absolute left-3 top-3 bg-background/70 px-2 py-1 font-mono text-[9px] font-bold backdrop-blur">
                    REF: {slotB.ref}
                  </div>
                  <div className="mt-4 flex justify-between text-xs">
                    <span className="font-bold uppercase">{slotB.name}</span>
                    <span className="font-mono text-neon-cyan">${slotB.price.toFixed(2)}</span>
                  </div>
                </Link>
              )}
            </div>
          </div>
        </section>
      )}
      {deals && deals.length > 0 && (
        <>
          <SectionDivider title="On|Sale" accent="text-neon-pink" linkTo="/shop?sort=discount" />
          <ProductGridSection products={deals} />
        </>
      )}
      {bestSellers && bestSellers.length > 0 && (
        <>
          <SectionDivider title="Most|Wanted" accent="text-neon-cyan" linkTo="/shop?sort=best_sellers" />
          <ProductGridSection products={bestSellers} />
        </>
      )}
      <section className="border-y border-border bg-card py-32">
        <div className="mx-auto max-w-[1440px] px-6 md:px-10">
          <h2 className="mt-6 max-w-5xl font-display text-5xl uppercase leading-[0.9] tracking-tighter md:text-7xl lg:text-8xl">
            Built for <span className="text-outline">motion</span>. Designed for the <span className="italic text-neon-pink">in-between</span>.
          </h2>
          <p className="mt-10 max-w-2xl text-lg leading-relaxed text-muted-foreground">
            VELO_CITI exists in the transit zone — between cities, between hours, between identities. Every garment is engineered as a tool for navigating the urban frequency.
          </p>
        </div>
      </section>
      {newArrivals && newArrivals.content.length > 0 && (
        <>
          <SectionDivider title="New|Arrivals" accent="text-neon-pink" linkTo="/shop?sort=recent" />
          <ProductGridSection products={newArrivals.content} />
        </>
      )}
      {topRated && topRated.length > 0 && (
        <>
          <SectionDivider title="Top|Rated" accent="text-neon-cyan" linkTo="/shop?sort=top_rated" />
          <ProductGridSection products={topRated} />
        </>
      )}
    </>
  )
}
