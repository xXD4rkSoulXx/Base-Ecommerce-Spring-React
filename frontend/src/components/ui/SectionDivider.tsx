import { Link } from 'react-router-dom'

interface SectionDividerProps {
  title: string
  accent: string
  linkTo?: string
  linkLabel?: string
}

export const SectionDivider = ({ title, accent, linkTo, linkLabel }: SectionDividerProps) => (
  <section className="border-y border-border bg-card">
    <div className="mx-auto flex max-w-[1440px] items-center justify-between px-6 py-8 md:px-10">
      <h2 className="font-display text-2xl uppercase italic tracking-tighter md:text-4xl">
        {title.split('|').map((part, i) =>
          i === 1 ? <span key={i} className={accent}>{part}</span> : part
        )}
      </h2>
      {linkTo && (
        <Link to={linkTo} className="text-[10px] font-bold uppercase tracking-[0.3em] hover:text-neon-cyan">
          {linkLabel ?? 'View All →'}
        </Link>
      )}
    </div>
  </section>
)
