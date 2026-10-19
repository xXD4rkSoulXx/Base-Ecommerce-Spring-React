export const Ticker = () => {
  const items = [
    'Shipping to selected countries in Europe and worldwide',
    'Live carrier rates at checkout',
    'Secure payments by Stripe',
    'Discount codes at checkout'
  ]
  const list = [...items, ...items]

  return (
    <div className="overflow-hidden border-y border-border bg-card py-3">
      <div className="flex animate-marquee gap-12 whitespace-nowrap">
        {list.map((item, i) => (
          <span
            key={`${item}-${i}`}
            className="flex shrink-0 items-center gap-12 text-[10px] font-bold uppercase tracking-[0.5em] text-muted-foreground"
          >
            {item}
            <span className="text-neon-pink" aria-hidden="true">●</span>
          </span>
        ))}
      </div>
    </div>
  )
}
