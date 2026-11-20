import { Link } from 'react-router-dom'
import { ProductCard } from '@/components/card/ProductCard'
import type { ProductResponse } from '@/types/product'

type ProductShelfAccent = 'pink' | 'cyan'

interface ProductShelfSectionProps {
  title: string
  highlight: string
  accent: ProductShelfAccent
  linkTo: string
  products: ProductResponse[] | undefined
  isLoading: boolean
}

const ACCENT_CLASS_NAMES: Record<ProductShelfAccent, string> = {
  pink: 'text-neon-pink',
  cyan: 'text-neon-cyan'
}

const SKELETON_ITEMS = Array.from({ length: 8 }, (_, index) => index)

export const ProductShelfSection = ({
  title,
  highlight,
  accent,
  linkTo,
  products,
  isLoading
}: ProductShelfSectionProps) => {
  if (!isLoading && (!products || products.length === 0)) {
    return null
  }

  return (
    <section>
      <div className="border-y border-border bg-card">
        <div className="mx-auto flex max-w-[1440px] items-center justify-between px-6 py-8 md:px-10">
          <h2 className="font-display text-2xl uppercase italic tracking-tighter md:text-4xl">
            {title}
            <span className={ACCENT_CLASS_NAMES[accent]}>{highlight}</span>
          </h2>
          <Link to={linkTo} className="text-[10px] font-bold uppercase tracking-[0.3em] hover:text-neon-cyan">
            View All<span className="sr-only"> {title} {highlight}</span> →
          </Link>
        </div>
      </div>
      <div className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
        <div className="grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-4 lg:gap-x-6">
          {isLoading
            ? SKELETON_ITEMS.map((item) => (
                <div key={item} aria-hidden="true" className="animate-pulse">
                  <div className="aspect-[3/4] border border-border bg-card" />
                  <div className="mt-4 h-11" />
                </div>
              ))
            : products?.map((product, index) => <ProductCard key={product.id} product={product} index={index} />)}
        </div>
      </div>
    </section>
  )
}
