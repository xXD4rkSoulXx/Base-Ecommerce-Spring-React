function Ticker() {
  const items = [
    "Next Discounts in 04:12:09:44",
    "Global Logistics Operational",
    "Berlin HQ Online",
    "Limited Edition Series V01–V04",
    "Free shipping over 200€"
  ];
  const list = [...items, ...items];

  return (
    <div className="overflow-hidden border-y border-border bg-card py-3">
      <div className="flex animate-marquee gap-12 whitespace-nowrap">
        {list.map((item, i) => (
          <span
            key={i}
            className="flex shrink-0 items-center gap-12 text-[10px] font-bold uppercase tracking-[0.5em] text-muted-foreground"
          >
            {item}
            <span className="text-neon-pink">●</span>
          </span>
        ))}
      </div>
    </div>
  );
}

export default Ticker
