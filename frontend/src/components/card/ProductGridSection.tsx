import { ProductCard } from '@/components/card/ProductCard'
import type { ProductResponse } from '@/types/product'

interface ProductGridSectionProps {
  products: ProductResponse[]
}

export const ProductGridSection = ({ products }: ProductGridSectionProps) => (
  <section className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
    <div className="grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-4 lg:gap-x-6">
      {products.map((product, i) => (
        <ProductCard key={product.id} product={product} index={i} />
      ))}
    </div>
  </section>
)
