import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { CHART_AXIS_PROPS, CHART_TOOLTIP_STYLE } from '@/utils/chart'
import { formatPrice } from '@/utils/format'
import type { AdminDashboardSalesPointResponse } from '@/types/admin/adminDashboard'

interface AdminDashboardSalesChartProps {
  data: AdminDashboardSalesPointResponse[]
}

const dayFormatter = new Intl.DateTimeFormat('pt-PT', { day: '2-digit', month: '2-digit' })

const formatDay = (date: string) => dayFormatter.format(new Date(`${date}T00:00:00`))

export const AdminDashboardSalesChart = ({ data }: AdminDashboardSalesChartProps) => (
  <div className="h-72 w-full">
    <ResponsiveContainer width="100%" height="100%">
      <LineChart data={data} margin={{ top: 8, right: 16, bottom: 0, left: 0 }}>
        <CartesianGrid stroke="var(--color-border)" vertical={false} />
        <XAxis dataKey="date" tickFormatter={formatDay} minTickGap={24} {...CHART_AXIS_PROPS} />
        <YAxis tickFormatter={(value: number) => formatPrice(value)} width={80} {...CHART_AXIS_PROPS} />
        <Tooltip
          formatter={(value) => formatPrice(Number(value))}
          labelFormatter={(label) => formatDay(String(label))}
          contentStyle={CHART_TOOLTIP_STYLE}
        />
        <Line type="monotone" dataKey="sales" name="Sales" stroke="var(--color-neon-cyan)" strokeWidth={2} dot={false} />
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
