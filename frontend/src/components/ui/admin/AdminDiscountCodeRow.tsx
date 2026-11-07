import type { AdminDiscountCodeResponse } from '@/types/admin/adminDiscountCode'

interface AdminDiscountCodeRowProps {
  discountCode: AdminDiscountCodeResponse
  canEdit: boolean
  isBusy: boolean
  onEdit: (discountCode: AdminDiscountCodeResponse) => void
  onToggle: (discountCode: AdminDiscountCodeResponse) => void
}

const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium' })

export const AdminDiscountCodeRow = ({
  discountCode,
  canEdit,
  isBusy,
  onEdit,
  onToggle
}: AdminDiscountCodeRowProps) => (
  <tr className={`border-b border-border last:border-b-0 ${discountCode.active ? '' : 'opacity-60'}`}>
    <td className="p-4 font-mono text-sm font-bold">{discountCode.code}</td>
    <td className="p-4 font-mono text-sm text-neon-pink">-{discountCode.discountPercentage}%</td>
    <td className="p-4 font-mono text-sm">{discountCode.orders}</td>
    <td className="p-4">
      <span
        className={`inline-block border px-2 py-0.5 font-mono text-[10px] uppercase tracking-widest ${
          discountCode.active ? 'border-neon-cyan text-neon-cyan' : 'border-border text-muted-foreground'
        }`}
      >
        {discountCode.active ? 'Active' : 'Inactive'}
      </span>
    </td>
    <td className="p-4 font-mono text-xs text-muted-foreground">
      {dateFormatter.format(new Date(discountCode.createdAt))}
    </td>
    <td className="p-4 text-right">
      {canEdit && (
        <div className="flex justify-end gap-4 text-xs font-bold uppercase tracking-widest">
          <button onClick={() => onEdit(discountCode)} className="hover:text-neon-cyan">
            Edit
          </button>
          <button
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
