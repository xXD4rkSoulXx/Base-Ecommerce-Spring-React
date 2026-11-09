import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

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
        <XAxis type="number" allowDecimals={false} tick={{ fill: 'var(--color-muted-foreground)', fontSize: 10 }} stroke="var(--color-border)" />
        <YAxis
          type="category"
          dataKey="label"
          width={120}
          tick={{ fill: 'var(--color-muted-foreground)', fontSize: 10 }}
          stroke="var(--color-border)"
        />
        <Tooltip
          cursor={{ fill: 'var(--color-muted)' }}
          contentStyle={{
            background: 'var(--color-popover)',
            border: '1px solid var(--color-border)',
            borderRadius: 0
          }}
        />
        <Bar dataKey="value" name={name} fill={color} />
      </BarChart>
    </ResponsiveContainer>
  </div>
)
