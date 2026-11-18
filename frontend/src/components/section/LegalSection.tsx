export interface LegalSectionProps {
  title: string
  paragraphs?: string[]
  items?: string[]
}

export const LegalSection = ({ title, paragraphs = [], items = [] }: LegalSectionProps) => (
  <section>
    <h2 className="font-display text-2xl uppercase tracking-tighter">{title}</h2>
    <div className="mt-3 space-y-3 text-sm leading-relaxed text-muted-foreground">
      {paragraphs.map((paragraph) => (
        <p key={paragraph}>{paragraph}</p>
      ))}
      {items.length > 0 && (
        <ul className="space-y-2">
          {items.map((item) => (
            <li key={item} className="flex gap-3">
              <span className="mt-2 h-1 w-1 shrink-0 bg-neon-cyan" />
              <span>{item}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  </section>
)