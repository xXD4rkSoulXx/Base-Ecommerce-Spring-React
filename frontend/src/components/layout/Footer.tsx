import { useState } from 'react'
import type { SubmitEvent } from 'react' 
import { NavLink } from 'react-router-dom'

export const Footer = () => {
  const [email, setEmail] = useState('')
  
  // Email subscription not implemented, maybe in a future update.
  const handleSubscribe = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (!email) {
      return
    }
    
    setEmail('')
  }

  return (
    <footer className="mt-32 border-t border-border bg-card">
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
              onSubmit={handleSubscribe}
              className="mt-8 flex max-w-md border border-border"
            >
              <input
                type="email"
                placeholder="your.email@signal.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
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
              <li><NavLink to="/shop" className="hover:text-neon-cyan">All</NavLink></li>
              <li><NavLink to="/shop?category=OUTERWEAR" className="hover:text-neon-cyan">Outerwear</NavLink></li>
              <li><NavLink to="/shop?category=TOPS" className="hover:text-neon-cyan">Tops</NavLink></li>
              <li><NavLink to="/shop?category=BOTTOMS" className="hover:text-neon-cyan">Bottoms</NavLink></li>
              <li><NavLink to="/shop?category=FOOTWEAR" className="hover:text-neon-cyan">Footwear</NavLink></li>
              <li><NavLink to="/shop?category=ACCESSORIES" className="hover:text-neon-cyan">Accessories</NavLink></li>
            </ul>
          </div>
          <div>
            <h4 className="mb-6 text-[10px] font-bold uppercase tracking-[0.3em] text-muted-foreground">
              Network
            </h4>
            <ul className="space-y-3 text-sm">
              <li><NavLink to="/about" className="hover:text-neon-cyan">About</NavLink></li>
              <li><NavLink to="/account" className="hover:text-neon-cyan">Account</NavLink></li>
              <li><NavLink to="/admin" className="hover:text-neon-cyan">Admin</NavLink></li>
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
  )
}
