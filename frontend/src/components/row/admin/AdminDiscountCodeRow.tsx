import { StatusBadge } from '@/components/badge/StatusBadge'
import { formatDate } from '@/utils/format'
import type { AdminDiscountCodeResponse } from '@/types/admin/adminDiscountCode'

interface AdminDiscountCodeRowProps {
  discountCode: AdminDiscountCodeResponse
  canEdit: boolean
  isBusy: boolean
  onEdit: (discountCode: AdminDiscountCodeResponse) => void
  onToggle: (discountCode: AdminDiscountCodeResponse) => void
}

export const AdminDiscountCodeRow = ({
  discountCode,
  canEdit,
  isBusy,
  onEdit,
  onToggle
}: AdminDiscountCodeRowProps) => (
  <tr className="border-b border-border last:border-b-0">
    <td className="p-4 font-mono text-sm font-bold">{discountCode.code}</td>
    <td className="p-4 font-mono text-sm text-neon-pink">-{discountCode.discountPercentage}%</td>
    <td className="p-4 font-mono text-sm">{discountCode.orders}</td>
    <td className="p-4">
      <StatusBadge tone={discountCode.active ? 'cyan' : 'muted'}>
        {discountCode.active ? 'Active' : 'Inactive'}
      </StatusBadge>
    </td>
    <td className="p-4 font-mono text-xs text-muted-foreground">{formatDate(discountCode.createdAt)}</td>
    <td className="p-4 text-right">
      {canEdit && (
        <div className="flex justify-end gap-4 text-xs font-bold uppercase tracking-widest">
          <button type="button" onClick={() => onEdit(discountCode)} className="hover:text-neon-cyan">
            Edit
          </button>
          <button
            type="button"
            onClick={() => onToggle(discountCode)}
            disabled={isBusy}
            className={`disabled:opacity-50 ${
              discountCode.active ? 'text-neon-pink hover:opacity-80' : 'hover:text-neon-cyan'
            }`}
          >
            {discountCode.active ? 'Deactivate' : 'Activate'}
          </button>
        </div>
      )}
    </td>
  </tr>
)
