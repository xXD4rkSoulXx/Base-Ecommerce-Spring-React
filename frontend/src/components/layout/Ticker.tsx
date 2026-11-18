const TICKER_ITEMS = [
  'Shipping to selected countries in Europe and worldwide',
  'Live carrier rates at checkout',
  'Secure payments by Stripe',
  'Discount codes at checkout'
]

const COPIES = [0, 1]

export const Ticker = () => (
  <div className="overflow-hidden border-y border-border bg-card py-3">
    <div className="flex w-max animate-marquee whitespace-nowrap motion-reduce:animate-none">
      {COPIES.map((copy) => (
        <div key={copy} aria-hidden={copy > 0} className={`flex shrink-0 ${copy > 0 ? 'motion-reduce:hidden' : ''}`}>
          {TICKER_ITEMS.map((item) => (
            <span
              key={item}
              className="flex shrink-0 items-center gap-12 pr-12 text-[10px] font-bold uppercase tracking-[0.5em] text-muted-foreground"
            >
              {item}
              <span className="text-neon-pink" aria-hidden="true">
                ●
              </span>
            </span>
          ))}
        </div>
      ))}
    </div>
  </div>
)
