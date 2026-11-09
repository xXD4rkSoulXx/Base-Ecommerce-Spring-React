import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { AdminDashboardSalesPointResponse } from '@/types/admin/adminDashboard'

interface AdminDashboardSalesChartProps {
  data: AdminDashboardSalesPointResponse[]
}

const currencyFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const dayFormatter = new Intl.DateTimeFormat('pt-PT', { day: '2-digit', month: '2-digit' })

const formatDay = (date: string) => dayFormatter.format(new Date(`${date}T00:00:00`))

export const AdminDashboardSalesChart = ({ data }: AdminDashboardSalesChartProps) => (
  <div className="h-72 w-full">
    <ResponsiveContainer width="100%" height="100%">
      <LineChart data={data} margin={{ top: 8, right: 16, bottom: 0, left: 0 }}>
        <CartesianGrid stroke="var(--color-border)" vertical={false} />
        <XAxis
          dataKey="date"
          tickFormatter={formatDay}
          tick={{ fill: 'var(--color-muted-foreground)', fontSize: 10 }}
          stroke="var(--color-border)"
          minTickGap={24}
        />
        <YAxis
          tickFormatter={(value: number) => currencyFormatter.format(value)}
          tick={{ fill: 'var(--color-muted-foreground)', fontSize: 10 }}
          stroke="var(--color-border)"
          width={80}
        />
        <Tooltip
          formatter={(value) => currencyFormatter.format(Number(value))}
          labelFormatter={(label) => formatDay(String(label))}
          contentStyle={{
            background: 'var(--color-popover)',
            border: '1px solid var(--color-border)',
            borderRadius: 0
          }}
        />
        <Line
          type="monotone"
          dataKey="sales"
          name="Sales"
          stroke="var(--color-neon-cyan)"
          strokeWidth={2}
          dot={false}
        />
        <Line
          type="monotone"
          dataKey="refunds"
          name="Refunds"
          stroke="var(--color-neon-pink)"
          strokeWidth={2}
          dot={false}
        />
      </LineChart>
    </ResponsiveContainer>
  </div>
)
