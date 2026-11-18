import { formatSize } from '@/utils/format'
import type { AdminStockItemResponse } from '@/types/admin/adminStock'

interface AdminStockRowProps {
  item: AdminStockItemResponse
  canEdit: boolean
  onEdit: (item: AdminStockItemResponse) => void
}

export const AdminStockRow = ({ item, canEdit, onEdit }: AdminStockRowProps) => (
  <tr className="border-b border-border last:border-b-0">
    <td className="p-4">
      <div className="flex items-center gap-3">
        <div className="h-12 w-12 shrink-0 overflow-hidden border border-border bg-background">
          {item.cover && <img src={item.cover} alt="" loading="lazy" className="h-full w-full object-cover" />}
        </div>
        <div className="min-w-0">
          <p className="truncate font-bold">{item.productName}</p>
          <p className="font-mono text-[10px] text-muted-foreground">{item.productRef}</p>
        </div>
      </div>
    </td>
    <td className="p-4 font-mono text-xs">
      {formatSize(item.size)} · {item.color}
    </td>
    <td className="p-4 text-right font-mono">
      <span className={item.lowStock ? 'text-neon-pink' : undefined}>{item.stock}</span>
      {item.stock === 0 && <p className="text-[10px] uppercase tracking-widest text-neon-pink">Out of stock</p>}
      {item.stock > 0 && item.lowStock && (
        <p className="text-[10px] uppercase tracking-widest text-neon-pink">Low stock</p>
      )}
    </td>
    <td className="p-4 text-right">
      {canEdit && (
        <button
          type="button"
          onClick={() => onEdit(item)}
          className="border border-neon-cyan/50 px-4 py-2 font-mono text-[10px] uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan/10"
        >
          Update
        </button>
      )}
    </td>
  </tr>
)
