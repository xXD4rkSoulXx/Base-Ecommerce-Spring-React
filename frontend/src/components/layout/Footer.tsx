import { Link } from "react-router-dom"

function Footer() {
  return (
    <footer className="border-t border-border bg-card mt-32">
      <div className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
        <div className="grid gap-12 md:grid-cols-4">
          <div className="md:col-span-2">
            <h3 className="font-display text-5xl italic tracking-tighter md:text-7xl">
              JOIN THE <span className="text-neon-cyan">SIGNAL</span>
            </h3>
            <p className="mt-4 max-w-md text-sm text-muted-foreground">
              Drop alerts, product files, and access to private archives. Sent monthly. Never sold.
            </p>
            <form
              onSubmit={(e) => e.preventDefault()}
              className="mt-8 flex max-w-md border border-border"
            >
              <input
                type="email"
                placeholder="your.email@signal.com"
                className="flex-1 bg-transparent px-4 py-3 text-sm placeholder:text-muted-foreground focus:outline-none"
                required
              />
              <button
                type="submit"
                className="bg-neon-cyan px-6 text-xs font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80"
              >
                Subscribe
              </button>
            </form>
          </div>
          <div>
            <h4 className="mb-6 text-[10px] font-bold uppercase tracking-[0.3em] text-muted-foreground">
              Shop
            </h4>
            <ul className="space-y-3 text-sm">
              <li><Link to="/shop" className="hover:text-neon-cyan">All</Link></li>
              <li><Link to="/shop?category=Outerwear" className="hover:text-neon-cyan">Outerwear</Link></li>
              <li><Link to="/shop?category=Tops" className="hover:text-neon-cyan">Tops</Link></li>
              <li><Link to="/shop?category=Bottoms" className="hover:text-neon-cyan">Bottoms</Link></li>
              <li><Link to="/shop?category=Footwear" className="hover:text-neon-cyan">Footwear</Link></li>
              <li><Link to="/shop?category=Accessories" className="hover:text-neon-cyan">Accessories</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="mb-6 text-[10px] font-bold uppercase tracking-[0.3em] text-muted-foreground">
              Network
            </h4>
            <ul className="space-y-3 text-sm">
              <li><Link to="/about" className="hover:text-neon-cyan">About</Link></li>
              <li><Link to="/account" className="hover:text-neon-cyan">Account</Link></li>
              <li><Link to="/admin" className="hover:text-neon-cyan">Admin</Link></li>
            </ul>
          </div>
        </div>
        <div className="mt-20 flex flex-col gap-4 border-t border-border pt-8 md:flex-row md:items-center md:justify-between">
          <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            © 2026 VELO_CITI / All Frequencies Reserved
          </p>
          <p className="font-display text-[10px] uppercase tracking-[0.5em] text-neon-cyan">
            Berlin · Tokyo · Seoul
          </p>
        </div>
      </div>
    </footer>
  );
}

export default Footer
