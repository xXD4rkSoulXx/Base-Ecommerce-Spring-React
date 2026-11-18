import { Link } from 'react-router-dom'
import type { AdminDashboardTopProductResponse } from '@/types/admin/adminDashboard'

interface AdminDashboardTopProductsSectionProps {
  products: AdminDashboardTopProductResponse[]
}

export const AdminDashboardTopProductsSection = ({ products }: AdminDashboardTopProductsSectionProps) => (
  <ul>
    {products.map((product) => (
      <li
        key={product.productId}
        className="flex items-center gap-4 border-b border-border p-4 last:border-b-0"
      >
        {product.cover ? (
          <img src={product.cover} alt="" className="h-12 w-12 border border-border object-cover" />
        ) : (
          <div className="h-12 w-12 border border-border bg-muted" />
        )}
        <div className="min-w-0 flex-1">
          <Link
            to={`/admin/products/${product.productId}`}
            className="block truncate font-bold hover:text-neon-cyan"
          >
            {product.name}
          </Link>
          <p className="font-mono text-xs text-muted-foreground">{product.ref}</p>
        </div>
        <p className="font-mono text-sm text-neon-cyan">{product.unitsSold}</p>
      </li>
    ))}
  </ul>
)
