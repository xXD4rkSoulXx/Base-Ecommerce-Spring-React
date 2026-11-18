import { ArrowUpRight } from 'lucide-react'
import { APP_NAME } from '@/constants'

const REPOSITORY_URL = 'https://github.com/xXD4rkSoulXx/Base-Ecommerce-Spring-React'

const FEATURES = [
  {
    number: '01',
    title: 'Real checkout',
    body: 'Cart, discount codes and hosted payment with Stripe in test mode, with tax calculated at checkout.'
  },
  {
    number: '02',
    title: 'Real shipping rates',
    body: 'Shipping options come from Shippo for your address. Larger orders are packed into several boxes, each with its own simulated tracking.'
  },
  {
    number: '03',
    title: 'Returns that work',
    body: 'Return items within 14 days, with partial refunds that include a proportional share of tax and shipping.'
  }
]

const STACK = [
  'Spring Boot',
  'PostgreSQL',
  'JWT auth',
  'Stripe',
  'Shippo',
  'React',
  'TypeScript',
  'TanStack Query',
  'Zustand',
  'Tailwind',
  'Vercel',
  'Render',
  'Cloudinary',
  'Supabase'
]

export const About = () => (
  <div className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
    <h1 className="mt-4 max-w-5xl font-display text-6xl uppercase leading-[0.85] tracking-tighter md:text-8xl lg:text-[140px]">
      Built to show the <span className="text-outline">whole</span> <span className="italic text-neon-pink">stack</span>
      .
    </h1>
    <p className="mt-10 max-w-2xl text-sm text-muted-foreground">
      {APP_NAME} is a portfolio project: a complete online store with a Spring Boot API and a React storefront.
      Everything you can click is real code. The products, the brand and the money are not.
    </p>
    <div className="mt-20 grid gap-12 md:grid-cols-3">
      {FEATURES.map((feature) => (
        <div key={feature.number}>
          <p className="font-mono text-5xl text-neon-cyan">{feature.number}</p>
          <h2 className="mt-4 font-display text-2xl uppercase tracking-tighter">{feature.title}</h2>
          <p className="mt-3 text-sm text-muted-foreground">{feature.body}</p>
        </div>
      ))}
    </div>
    <div className="mt-20">
      <h2 className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Under the hood</h2>
      <ul className="mt-4 flex flex-wrap gap-2">
        {STACK.map((item) => (
          <li key={item} className="border border-border px-3 py-1.5 font-mono text-[10px] uppercase tracking-widest">
            {item}
          </li>
        ))}
      </ul>
    </div>
    <a
      href={REPOSITORY_URL}
      target="_blank"
      rel="noreferrer"
      className="mt-12 inline-flex items-center gap-2 border border-neon-cyan px-5 py-3 font-mono text-xs uppercase tracking-widest text-neon-cyan transition-colors hover:bg-neon-cyan hover:text-primary-foreground"
    >
      View the source
      <ArrowUpRight className="h-4 w-4" />
    </a>
  </div>
)
