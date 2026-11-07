import { Link } from 'react-router-dom'
import type { AdminCustomerResponse } from '@/types/admin/adminCustomer'

interface AdminCustomerRowProps {
  customer: AdminCustomerResponse
}

const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium' })

export const AdminCustomerRow = ({ customer }: AdminCustomerRowProps) => (
  <tr className="border-b border-border last:border-b-0">
    <td className="p-4">
      <p className="font-bold">
        {customer.firstName} {customer.lastName}
      </p>
      <p className="font-mono text-xs text-muted-foreground">{customer.email}</p>
    </td>
    <td className="p-4 font-mono text-xs text-muted-foreground">
      {dateFormatter.format(new Date(customer.createdAt))}
    </td>
    <td className="p-4 font-mono text-sm">{customer.orders}</td>
    <td className="p-4 font-mono text-xs text-muted-foreground">
      {customer.lastOrderAt ? dateFormatter.format(new Date(customer.lastOrderAt)) : '—'}
    </td>
    <td className="p-4 text-right">
      <Link
        to={`/admin/customers/${customer.id}`}
        className="text-xs font-bold uppercase tracking-widest hover:text-neon-cyan"
      >
        View →
      </Link>
    </td>
  </tr>
)
