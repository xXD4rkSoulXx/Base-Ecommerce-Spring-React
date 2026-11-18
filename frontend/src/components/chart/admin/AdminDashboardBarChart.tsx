import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { CHART_AXIS_PROPS, CHART_TOOLTIP_STYLE } from '@/utils/chart'

interface AdminDashboardBarChartProps {
  data: { label: string; value: number }[]
  name: string
  color: string
}

export const AdminDashboardBarChart = ({ data, name, color }: AdminDashboardBarChartProps) => (
  <div className="h-72 w-full p-4">
    <ResponsiveContainer width="100%" height="100%">
      <BarChart data={data} layout="vertical" margin={{ top: 0, right: 16, bottom: 0, left: 0 }}>
        <CartesianGrid stroke="var(--color-border)" horizontal={false} />
        <XAxis type="number" allowDecimals={false} {...CHART_AXIS_PROPS} />
        <YAxis type="category" dataKey="label" width={120} {...CHART_AXIS_PROPS} />
        <Tooltip cursor={{ fill: 'var(--color-muted)' }} contentStyle={CHART_TOOLTIP_STYLE} />
        <Bar dataKey="value" name={name} fill={color} />
      </BarChart>
    </ResponsiveContainer>
  </div>
)
