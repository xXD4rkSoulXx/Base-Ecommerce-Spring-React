import { pluralize } from '@/utils/format'
import type { AdminDashboardDiscountCodeResponse } from '@/types/admin/adminDashboard'

interface AdminDashboardDiscountCodesSectionProps {
  codes: AdminDashboardDiscountCodeResponse[]
}

export const AdminDashboardDiscountCodesSection = ({ codes }: AdminDashboardDiscountCodesSectionProps) => (
  <ul>
    {codes.map((discountCode) => (
      <li
        key={discountCode.code}
        className="flex items-center justify-between border-b border-border p-4 last:border-b-0"
      >
        <p className="font-mono font-bold">{discountCode.code}</p>
        <p className="font-mono text-sm text-neon-cyan">{pluralize(discountCode.orders, 'order')}</p>
      </li>
    ))}
  </ul>
)
