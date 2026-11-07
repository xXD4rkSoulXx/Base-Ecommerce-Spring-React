import { Link } from 'react-router-dom'
import { FeaturedProductCard } from '@/components/card/FeaturedProductCard'
import { SectionDivider } from '@/components/ui/SectionDivider'
import { ProductGridSection } from '@/components/section/ProductGridSection'
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

  const left = featured?.find((product) => product.featuredSlot === 'LEFT')
  const right = featured?.find((product) => product.featuredSlot === 'RIGHT')

  return (
    <>
      {(left || right) && (
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
            <div
              className={`col-span-12 grid gap-4 lg:col-span-7 lg:gap-6 ${left && right ? 'sm:grid-cols-2' : 'grid-cols-1'}`}
            >
              {left && <FeaturedProductCard product={left} />}
              {right && <FeaturedProductCard product={right} secondary />}
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
