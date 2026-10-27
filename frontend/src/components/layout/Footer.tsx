import { NavLink } from 'react-router-dom'

export const Footer = () => (
  <footer className="mt-32 border-t border-border bg-card">
    <div className="mx-auto max-w-[1440px] px-6 py-20 md:px-10">
      <div className="grid gap-12 md:grid-cols-5">
        <div className="md:col-span-2">
          <h3 className="font-display text-5xl italic tracking-tighter md:text-7xl">
            THIS IS A <span className="text-neon-cyan">DEMO</span>
          </h3>
          <p className="mt-4 max-w-md text-sm text-muted-foreground">
            VELO_CITI is a portfolio project, not a real store. Payments run in Stripe test mode,
            shipping labels and tracking are simulated, and no order will ever be shipped.
          </p>
        </div>
        <div>
          <h4 className="mb-6 text-[10px] font-bold uppercase tracking-[0.3em] text-muted-foreground">
            Shop
          </h4>
          <ul className="space-y-3 text-sm">
            <li><NavLink to="/shop" end className="hover:text-neon-cyan">All</NavLink></li>
            <li><NavLink to="/shop?category=OUTERWEAR" end className="hover:text-neon-cyan">Outerwear</NavLink></li>
            <li><NavLink to="/shop?category=TOPS" end className="hover:text-neon-cyan">Tops</NavLink></li>
            <li><NavLink to="/shop?category=BOTTOMS" end className="hover:text-neon-cyan">Bottoms</NavLink></li>
            <li><NavLink to="/shop?category=FOOTWEAR" end className="hover:text-neon-cyan">Footwear</NavLink></li>
            <li><NavLink to="/shop?category=ACCESSORIES" end className="hover:text-neon-cyan">Accessories</NavLink></li>
          </ul>
        </div>
        <div>
          <h4 className="mb-6 text-[10px] font-bold uppercase tracking-[0.3em] text-muted-foreground">
            Network
          </h4>
          <ul className="space-y-3 text-sm">
            <li><NavLink to="/about" end className="hover:text-neon-cyan">About</NavLink></li>
            <li><NavLink to="/account" end className="hover:text-neon-cyan">Account</NavLink></li>
          </ul>
        </div>
        <div>
          <h4 className="mb-6 text-[10px] font-bold uppercase tracking-[0.3em] text-muted-foreground">
            Support
          </h4>
          <ul className="space-y-3 text-sm">
            <li><NavLink to="/returns" end className="hover:text-neon-cyan">Returns</NavLink></li>
            <li><NavLink to="/faq" end className="hover:text-neon-cyan">FAQ</NavLink></li>
            <li><NavLink to="/terms" end className="hover:text-neon-cyan">Terms</NavLink></li>
            <li><NavLink to="/privacy" end className="hover:text-neon-cyan">Privacy</NavLink></li>
          </ul>
        </div>
      </div>
      <div className="mt-20 flex flex-col gap-4 border-t border-border pt-8 md:flex-row md:items-center md:justify-between">
        <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          © 2026 VELO_CITI / All Frequencies Reserved
        </p>
        <p className="font-display text-[10px] uppercase tracking-[0.5em] text-neon-cyan">
          Berlin
        </p>
      </div>
    </div>
  </footer>
)
