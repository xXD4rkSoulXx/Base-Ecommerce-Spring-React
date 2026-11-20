import { Link } from 'react-router-dom'
import { APP_NAME, PRODUCT_CATEGORIES } from '@/constants'
import { formatCategory } from '@/utils/format'

interface FooterLink {
  to: string
  label: string
}

interface FooterColumn {
  title: string
  links: FooterLink[]
}

const FOOTER_COLUMNS: FooterColumn[] = [
  {
    title: 'Shop',
    links: [
      { to: '/shop', label: 'All' },
      ...PRODUCT_CATEGORIES.map((category) => ({
        to: `/shop?category=${category}`,
        label: formatCategory(category)
      }))
    ]
  },
  {
    title: 'Network',
    links: [
      { to: '/about', label: 'About' },
      { to: '/account', label: 'Account' }
    ]
  },
  {
    title: 'Support',
    links: [
      { to: '/returns', label: 'Returns' },
      { to: '/faq', label: 'FAQ' },
      { to: '/terms', label: 'Terms' },
      { to: '/privacy', label: 'Privacy' }
    ]
  }
]

export const Footer = () => (
  <footer className="mt-32 border-t border-border bg-card">
    <div className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
      <div className="grid gap-12 md:grid-cols-5">
        <div className="md:col-span-2">
          <h2 className="font-display text-5xl italic tracking-tighter md:text-7xl">
            THIS IS A <span className="text-neon-cyan">DEMO</span>
          </h2>
          <p className="mt-4 max-w-md text-sm text-muted-foreground">
            {APP_NAME} is a portfolio project, not a real store. Payments run in Stripe test mode, shipping labels and
            tracking are simulated, and no order will ever be shipped.
          </p>
        </div>
        {FOOTER_COLUMNS.map((column) => (
          <div key={column.title}>
            <h3 className="mb-6 text-[10px] font-bold uppercase tracking-[0.3em] text-muted-foreground">
              {column.title}
            </h3>
            <ul className="space-y-3 text-sm">
              {column.links.map((link) => (
                <li key={link.to}>
                  <Link to={link.to} className="hover:text-neon-cyan">
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <div className="mt-20 flex flex-col gap-4 border-t border-border pt-8 md:flex-row md:items-center md:justify-between">
        <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          © {new Date().getFullYear()} {APP_NAME} / All Frequencies Reserved
        </p>
        <p className="font-display text-[10px] uppercase tracking-[0.5em] text-neon-cyan">Berlin</p>
      </div>
    </div>
  </footer>
)
