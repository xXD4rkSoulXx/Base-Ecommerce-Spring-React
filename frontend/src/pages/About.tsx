export const About = () => {
  const perks: [string, string, string][] = [
    ['01', 'Engineered', 'Every garment is constructed from technical materials sourced from Berlin, Tokyo, and Seoul.'],
    ['02', 'Limited', 'Production runs are capped at 200 units. When the archive sells out, the object is retired.'],
    ['03', 'Transparent', 'Each piece carries a REF number that traces back to its full production history.']
  ]

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
      <h1 className="mt-4 max-w-5xl font-display text-6xl uppercase leading-[0.85] tracking-tighter md:text-8xl lg:text-[140px]">
        We design <span className="text-outline">tools</span> for the <span className="italic text-neon-pink">in-between</span>.
      </h1>
      <div className="mt-20 grid gap-12 md:grid-cols-3">
        {perks.map(([num, title, body]) => (
          <div key={num}>
            <p className="font-mono text-5xl text-neon-cyan">{num}</p>
            <h3 className="mt-4 font-display text-2xl uppercase tracking-tighter">{title}</h3>
            <p className="mt-3 text-sm text-muted-foreground">{body}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
