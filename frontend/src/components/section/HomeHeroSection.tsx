import { Link } from 'react-router-dom'
import { FeaturedProductCard } from '@/components/card/FeaturedProductCard'
import type { ProductResponse } from '@/types/product'

interface HomeHeroSectionProps {
  products: ProductResponse[] | undefined
  isLoading: boolean
}

export const HomeHeroSection = ({ products, isLoading }: HomeHeroSectionProps) => {
  const left = products?.find((product) => product.featuredSlot === 'LEFT')
  const right = products?.find((product) => product.featuredSlot === 'RIGHT')

  if (!isLoading && !left && !right) {
    return null
  }

  const hasTwoColumns = isLoading || (left && right)

  return (
    <section className="grid-noise relative overflow-hidden">
      <div aria-hidden="true" className="pointer-events-none absolute -left-10 -top-20 select-none opacity-[0.04]">
        <p className="font-display text-[28vw] uppercase leading-none tracking-tighter">SYSTEM</p>
      </div>
      <div className="relative mx-auto grid max-w-[1440px] grid-cols-12 gap-6 px-6 pb-32 pt-16 md:px-10">
        <div className="col-span-12 flex flex-col justify-end pb-8 lg:col-span-5">
          <h1 className="font-display text-7xl uppercase leading-[0.85] tracking-tighter md:text-8xl lg:text-[clamp(3.5rem,6vw,6.875rem)]">
            Raw <br />
            <span className="italic text-neon-cyan">Urban</span> <br />
            Static
          </h1>
          <p className="mt-8 max-w-md text-base leading-relaxed text-muted-foreground md:text-lg">
            Engineered for the high-velocity transition from daylight to deep neon. Reflective fibers. Concrete-grade
            textiles.
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
          className={`col-span-12 grid gap-4 lg:col-span-7 lg:gap-6 ${hasTwoColumns ? 'sm:grid-cols-2' : 'grid-cols-1'}`}
        >
          {isLoading && (
            <>
              <div className="aspect-[3/4] animate-pulse border border-border bg-card" />
              <div className="aspect-[3/4] animate-pulse border border-border bg-card" />
            </>
          )}
          {left && <FeaturedProductCard product={left} />}
          {right && <FeaturedProductCard product={right} secondary />}
        </div>
      </div>
    </section>
  )
}
