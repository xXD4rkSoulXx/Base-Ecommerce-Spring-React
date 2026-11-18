import { Link } from 'react-router-dom'
import { StatusBadge } from '@/components/badge/StatusBadge'
import { formatSize } from '@/utils/format'
import type { AdminProductSkuResponse } from '@/types/admin/adminProductSku'

interface AdminVariantRowProps {
  variant: AdminProductSkuResponse
  productRef: string
  canEdit: boolean
  canViewStock: boolean
  isBusy: boolean
  onImages: (variant: AdminProductSkuResponse) => void
  onEdit: (variant: AdminProductSkuResponse) => void
  onArchive: (variant: AdminProductSkuResponse) => void
  onRestore: (variant: AdminProductSkuResponse) => void
}

export const AdminVariantRow = ({
  variant,
  productRef,
  canEdit,
  canViewStock,
  isBusy,
  onImages,
  onEdit,
  onArchive,
  onRestore
}: AdminVariantRowProps) => (
  <tr className={`border-b border-border last:border-b-0 ${variant.active ? '' : 'opacity-60'}`}>
    <td className="p-4">
      <div className="flex items-center gap-3">
        <div className="h-12 w-12 shrink-0 overflow-hidden border border-border bg-background">
          {variant.cover && <img src={variant.cover} alt="" loading="lazy" className="h-full w-full object-cover" />}
        </div>
        <div>
          <p className="font-mono text-xs">
            {formatSize(variant.size)} · {variant.color}
          </p>
          {!variant.active && (
            <div className="mt-1">
              <StatusBadge tone="muted">Archived</StatusBadge>
            </div>
          )}
        </div>
      </div>
    </td>
    <td className="p-4 text-right font-mono">
      <p>{variant.stock}</p>
      {canViewStock && (
        <Link
          to={`/admin/stock?q=${productRef}`}
          className="text-[10px] uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
        >
          Manage →
        </Link>
      )}
    </td>
    <td className="p-4 font-mono text-xs">{variant.weightGrams} g</td>
    <td className="p-4 font-mono text-xs">
      {variant.lengthCm} × {variant.widthCm} × {variant.heightCm} cm
    </td>
    <td className="p-4 font-mono text-xs">
      {variant.discountPercentage > 0 ? (
        <span className="text-neon-pink">-{variant.discountPercentage}%</span>
      ) : (
        <span className="text-muted-foreground">—</span>
      )}
    </td>
    <td className="p-4 text-right">
      {canEdit && (
        <div className="flex justify-end gap-4 text-xs font-bold uppercase tracking-widest">
          <button type="button" onClick={() => onImages(variant)} className="hover:text-neon-cyan">
            Images
          </button>
          <button type="button" onClick={() => onEdit(variant)} className="hover:text-neon-cyan">
            Edit
          </button>
          {variant.active ? (
            <button
              type="button"
              onClick={() => onArchive(variant)}
              disabled={isBusy}
              className="text-neon-pink hover:opacity-80 disabled:opacity-50"
            >
              Archive
            </button>
          ) : (
            <button
              type="button"
              onClick={() => onRestore(variant)}
              disabled={isBusy}
              className="hover:text-neon-cyan disabled:opacity-50"
            >
              Restore
            </button>
          )}
        </div>
      )}
    </td>
  </tr>
)
