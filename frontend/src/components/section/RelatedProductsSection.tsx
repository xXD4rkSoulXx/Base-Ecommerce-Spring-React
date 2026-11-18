import { ProductCard } from '@/components/card/ProductCard'
import type { ProductResponse } from '@/types/product'

interface RelatedProductsSectionProps {
  products: ProductResponse[] | undefined
}

export const RelatedProductsSection = ({ products }: RelatedProductsSectionProps) => {
  if (!products || products.length === 0) {
    return null
  }

  return (
    <section className="mx-auto max-w-[1440px] border-t border-border px-6 py-20 md:px-10">
      <h2 className="mb-10 font-display text-3xl uppercase italic tracking-tighter md:text-5xl">
        Related <span className="text-neon-cyan">Objects</span>
      </h2>
      <div className="grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-4 lg:gap-x-6">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} index={0} />
        ))}
      </div>
    </section>
  )
}
