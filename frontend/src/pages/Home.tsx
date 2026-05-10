import { Link } from "react-router-dom";
import { useProducts } from "@/hooks/useProducts";
import Loading from "@/pages/Loading";
import Error from "@/pages/Error";
import { ProductCard } from "@/components/product/ProductCard";

export default function Home() {
  const {data: products, isLoading, error} = useProducts();

  if (isLoading) {
    return <Loading />;
  }

  if (error || !products || products.length === 0) {
    return <Error />;
  }
  
  const featured = products.filter((p) => p.featured).slice(0, 2);
  const newArrivals = products
      .filter(p => new Date(p.createdAt) >= new Date(Date.now() - 30 * 24 * 60 * 60 * 1000))
      .slice(0, 4);
  const discounts = products.filter((p) => p.discount).slice(0, 4);


  return (
    <>
      <section className="relative overflow-hidden grid-noise">
        <div className="pointer-events-none absolute -left-10 -top-20 select-none opacity-[0.04]">
          <h1 className="font-display text-[28vw] uppercase leading-none tracking-tighter">
            SYSTEM
          </h1>
        </div>

        <div className="relative mx-auto grid max-w-[1440px] grid-cols-12 gap-6 px-6 pb-32 pt-16 md:px-10">
          <div className="col-span-12 flex flex-col justify-end pb-8 lg:col-span-5">
            <h1 className="font-display text-7xl uppercase leading-[0.85] tracking-tighter md:text-8xl lg:text-[110px]">
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
              <Link
                to="/shop"
                className="border border-border px-8 py-4 text-xs font-black uppercase tracking-widest transition-colors hover:border-foreground md:px-12 md:py-5"
              >
                Lookbook
              </Link>
            </div>
          </div>

          <div className="col-span-12 grid grid-cols-2 gap-4 lg:col-span-7 lg:gap-6">
            <div className="relative">
              <div className="aspect-[3/4] overflow-hidden border border-border bg-card">
                <img
                  src={featured[0].image}
                  alt={featured[0].name}
                  loading="lazy"
                  className="h-full w-full object-cover grayscale-[30%]"
                />
              </div>
            </div>
            <div className="relative translate-y-12 lg:translate-y-24">
              <div className="aspect-[3/4] overflow-hidden border border-border bg-card">
                <img
                  src={featured[1].image}
                  alt={featured[1].name}
                  loading="lazy"
                  className="h-full w-full object-cover grayscale-[30%]"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="border-y border-border bg-card">
        <div className="mx-auto flex max-w-[1440px] items-center justify-between px-6 py-8 md:px-10">
          <h2 className="font-display text-2xl uppercase italic tracking-tighter md:text-4xl">
            Latest <span className="text-neon-cyan">Frequencies</span>
          </h2>
          <Link
            to="/shop"
            className="text-[10px] font-bold uppercase tracking-[0.3em] hover:text-neon-cyan"
          >
            All Objects →
          </Link>
        </div>
      </section>

      <section className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
        <div className="grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-4 lg:gap-x-6">
          {newArrivals.map((p, i) => (
            <ProductCard key={p.id} product={p} index={i} />
          ))}
        </div>
      </section>

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

      <section className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
        <div className="mb-12 flex items-end justify-between">
          <div>
            <h2 className="mt-3 font-display text-4xl uppercase tracking-tighter md:text-6xl">
              Discounts
            </h2>
          </div>
          <Link to="/shop" className="text-[10px] font-bold uppercase tracking-[0.3em] hover:text-neon-cyan">
            View All →
          </Link>
        </div>
        <div className="grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-4 lg:gap-x-6">
          {discounts.map((p, i) => (
            <ProductCard key={p.id} product={p} index={i} />
          ))}
        </div>
      </section>
    </>
  )
}