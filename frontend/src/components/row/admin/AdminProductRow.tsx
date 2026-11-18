import { Link } from 'react-router-dom'
import { formatCategory, formatPrice, pluralize } from '@/utils/format'
import type { AdminProductSummaryResponse } from '@/types/admin/adminProduct'

interface AdminProductRowProps {
  product: AdminProductSummaryResponse
  canEdit: boolean
}

export const AdminProductRow = ({ product, canEdit }: AdminProductRowProps) => (
  <tr className="border-b border-border last:border-b-0">
    <td className="p-4">
      <div className="flex items-center gap-3">
        <div className="h-12 w-12 shrink-0 overflow-hidden border border-border bg-background">
          {product.cover && <img src={product.cover} alt="" loading="lazy" className="h-full w-full object-cover" />}
        </div>
        <div className="min-w-0">
          <p className="truncate font-bold">{product.name}</p>
          <p className="font-mono text-[10px] text-muted-foreground">{product.ref}</p>
        </div>
      </div>
    </td>
    <td className="p-4 text-xs">{formatCategory(product.category)}</td>
    <td className="p-4 text-right font-mono">
      {formatPrice(product.price)}
      {product.discountPercentage > 0 && (
        <span className="ml-2 text-xs text-neon-pink">-{product.discountPercentage}%</span>
      )}
    </td>
    <td className="p-4 text-right font-mono">
      <p>{product.totalStock}</p>
      <p className="text-[10px] text-muted-foreground">{pluralize(product.skuCount, 'variant')}</p>
    </td>
    <td className="p-4">
      <div className="flex flex-wrap gap-2 font-mono text-[10px] uppercase tracking-widest">
        {!product.active && <span className="text-muted-foreground">Archived</span>}
        {product.featuredSlot && (
          <span className="text-neon-cyan">Featured · {product.featuredSlot === 'LEFT' ? 'Left' : 'Right'}</span>
        )}
        {product.lowStockSkus > 0 && <span className="text-neon-pink">Low stock</span>}
        {product.skuCount === 0 && <span className="text-neon-pink">No variants</span>}
      </div>
    </td>
    <td className="p-4 text-right">
      <Link
        to={`/admin/products/${product.id}`}
        className="text-xs font-bold uppercase tracking-widest hover:text-neon-cyan"
      >
        {canEdit ? 'Edit →' : 'View →'}
      </Link>
    </td>
  </tr>
)
