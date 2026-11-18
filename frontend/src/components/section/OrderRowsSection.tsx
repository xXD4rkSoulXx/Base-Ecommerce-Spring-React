import type { ReactNode } from 'react'
import { SectionTitle } from '@/components/ui/SectionTitle'

interface OrderRowsSectionProps {
  icon: ReactNode
  title: string
  count: number
  as?: 'h2' | 'h3'
  children: ReactNode
}

export const OrderRowsSection = ({ icon, title, count, as, children }: OrderRowsSectionProps) => (
  <div>
    <SectionTitle icon={icon} as={as}>
      {title} ({count})
    </SectionTitle>
    <div className="border border-border">{children}</div>
  </div>
)
