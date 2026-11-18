import { Link } from 'react-router-dom'

interface CheckoutBlockedSectionProps {
  title: string
  description?: string
  actionLabel: string
  to: string
}

export const CheckoutBlockedSection = ({ title, description, actionLabel, to }: CheckoutBlockedSectionProps) => (
  <div className="mx-auto max-w-2xl px-6 py-32 text-center">
    <h1 className="font-display text-3xl uppercase">{title}</h1>
    {description && <p className="mt-4 text-muted-foreground">{description}</p>}
    <Link
      to={to}
      className="mt-8 inline-block bg-neon-cyan px-8 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80"
    >
      {actionLabel}
    </Link>
  </div>
)
