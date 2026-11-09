import type { AdminDashboardDiscountCodeResponse } from '@/types/admin/adminDashboard'

interface AdminDashboardDiscountCodesProps {
  codes: AdminDashboardDiscountCodeResponse[]
}

export const AdminDashboardDiscountCodes = ({ codes }: AdminDashboardDiscountCodesProps) => (
  <ul>
    {codes.map((discountCode) => (
      <li
        key={discountCode.code}
        className="flex items-center justify-between border-b border-border p-4 last:border-b-0"
      >
        <p className="font-mono font-bold">{discountCode.code}</p>
        <p className="font-mono text-sm text-neon-cyan">
          {discountCode.orders} {discountCode.orders === 1 ? 'order' : 'orders'}
        </p>
      </li>
    ))}
  </ul>
)
