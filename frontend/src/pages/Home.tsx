import { HomeHeroSection } from '@/components/section/HomeHeroSection'
import { ProductShelfSection } from '@/components/section/ProductShelfSection'
import { APP_NAME } from '@/constants'
import { useBestSellers } from '@/hooks/useBestSellers'
import { useDealProducts } from '@/hooks/useDealProducts'
import { useFeaturedProducts } from '@/hooks/useFeaturedProducts'
import { useNewArrivals } from '@/hooks/useNewArrivals'
import { useTopRatedProducts } from '@/hooks/useTopRatedProducts'

export const Home = () => {
  const { data: featured } = useFeaturedProducts()
  const { data: deals } = useDealProducts()
  const { data: bestSellers } = useBestSellers()
  const { data: newArrivals } = useNewArrivals()
  const { data: topRated } = useTopRatedProducts()

  return (
    <>
      <HomeHeroSection products={featured} />
      <ProductShelfSection title="On" highlight="Sale" accent="pink" linkTo="/shop?sort=discount" products={deals} />
      <ProductShelfSection
        title="Most"
        highlight="Wanted"
        accent="cyan"
        linkTo="/shop?sort=best_sellers"
        products={bestSellers}
      />
      <section className="border-y border-border bg-card py-32">
        <div className="mx-auto max-w-[1440px] px-6 md:px-10">
          <h2 className="mt-6 max-w-5xl font-display text-5xl uppercase leading-[0.9] tracking-tighter md:text-7xl lg:text-8xl">
            Built for <span className="text-outline">motion</span>. Designed for the{' '}
            <span className="italic text-neon-pink">in-between</span>.
          </h2>
          <p className="mt-10 max-w-2xl text-lg leading-relaxed text-muted-foreground">
            {APP_NAME} exists in the transit zone — between cities, between hours, between identities. Every garment is
            engineered as a tool for navigating the urban frequency.
          </p>
        </div>
      </section>
      <ProductShelfSection
        title="New"
        highlight="Arrivals"
        accent="pink"
        linkTo="/shop?sort=recent"
        products={newArrivals}
      />
      <ProductShelfSection
        title="Top"
        highlight="Rated"
        accent="cyan"
        linkTo="/shop?sort=top_rated"
        products={topRated}
      />
    </>
  )
}
