import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { Heart, Menu, Search, ShoppingBag, User } from 'lucide-react'
import { Drawer } from '@/components/ui/Drawer'
import { UtcClock } from '@/components/ui/UtcClock'
import { APP_NAME } from '@/constants'
import { useCart } from '@/hooks/useCart'
import { usePermission } from '@/hooks/usePermission'
import { useWishlist } from '@/hooks/useWishlist'
import { useAuthStore } from '@/store/useAuthStore'

interface NavItem {
  to: string
  label: string
}

export const NavBar = () => {
  const [open, setOpen] = useState(false)
  const user = useAuthStore((state) => state.user)
  const { totalItems: cartCount } = useCart()
  const { totalItems: wishlistCount } = useWishlist()
  const canAccessAdmin = usePermission('DASHBOARD_READ')

  const navItems: NavItem[] = [
    { to: '/shop', label: 'Shop' },
    { to: '/about', label: 'About' },
    ...(canAccessAdmin ? [{ to: '/admin', label: 'Admin' }] : [])
  ]

  const closeMenu = () => setOpen(false)

  return (
    <>
      <nav className="sticky top-0 z-40 border-b border-border bg-background/85 backdrop-blur-xl">
        <div className="mx-auto flex max-w-[1440px] items-center justify-between px-6 py-4 md:px-10">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setOpen(true)}
              className="-m-2 p-2 text-foreground/80 transition-colors hover:text-neon-cyan xl:hidden"
              aria-label="Open menu"
            >
              <Menu className="h-5 w-5" />
            </button>
            <Link to="/" className="font-display text-2xl italic tracking-tighter">
              {APP_NAME}
            </Link>
          </div>
          <div className="hidden items-center gap-10 xl:flex">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end
                className={({ isActive }) =>
                  `text-xs font-bold uppercase tracking-[0.2em] transition-colors ${
                    isActive ? 'text-neon-cyan' : 'text-foreground hover:text-neon-cyan'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </div>
          <div className="flex items-center gap-4 md:gap-6">
            <UtcClock className="hidden md:inline" />
            <Link
              to="/search"
              className="-m-2 p-2 text-foreground/70 transition-colors hover:text-neon-cyan"
              aria-label="Search"
            >
              <Search className="h-4 w-4" />
            </Link>
            <Link
              to="/wishlist"
              className="relative -m-2 p-2 text-foreground/70 transition-colors hover:text-neon-pink"
              aria-label="Wishlist"
            >
              <Heart className="h-4 w-4" />
              {wishlistCount > 0 && (
                <span className="absolute right-0 top-0 flex h-4 min-w-4 items-center justify-center bg-neon-pink px-1 font-mono text-[9px] font-black text-primary-foreground">
                  {wishlistCount}
                </span>
              )}
            </Link>
            {user ? (
              <Link
                to="/account"
                className="-m-2 p-2 text-foreground/70 transition-colors hover:text-neon-cyan"
                aria-label="Account"
              >
                <User className="h-4 w-4" />
              </Link>
            ) : (
              <div className="hidden items-center gap-3 md:flex">
                <Link
                  to="/login"
                  className="text-[10px] font-bold uppercase tracking-[0.2em] text-foreground/80 transition-colors hover:text-neon-cyan"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="border border-neon-pink px-3 py-1.5 text-[10px] font-bold uppercase tracking-[0.2em] text-neon-pink transition-all hover:bg-neon-pink hover:text-primary-foreground"
                >
                  Register
                </Link>
              </div>
            )}
            <Link
              to="/cart"
              className="flex items-center gap-2 border border-neon-cyan px-3 py-1.5 text-[10px] font-bold uppercase tracking-[0.2em] text-neon-cyan transition-all hover:bg-neon-cyan hover:text-primary-foreground"
            >
              <ShoppingBag className="h-3 w-3" />
              <span>Cart [{String(cartCount).padStart(2, '0')}]</span>
            </Link>
          </div>
        </div>
      </nav>
      <Drawer open={open} onClose={closeMenu} label="Site menu" hideFrom="xl">
        <span className="font-display text-xl italic tracking-tighter">{APP_NAME}</span>
        <p className="mt-2 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Navigation</p>
        <nav className="mt-8 flex flex-col">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end
              onClick={closeMenu}
              className={({ isActive }) =>
                `border-b border-border py-4 text-sm font-bold uppercase tracking-[0.2em] transition-colors ${
                  isActive ? 'text-neon-cyan' : 'text-foreground hover:text-neon-cyan'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="mt-8 flex flex-col gap-4">
          {user ? (
            <Link
              to="/account"
              onClick={closeMenu}
              className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-foreground/80 hover:text-neon-cyan"
            >
              <User className="h-4 w-4" /> Account
            </Link>
          ) : (
            <>
              <Link
                to="/login"
                onClick={closeMenu}
                className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-foreground/80 hover:text-neon-cyan"
              >
                <User className="h-4 w-4" /> Login
              </Link>
              <Link
                to="/register"
                onClick={closeMenu}
                className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-foreground/80 hover:text-neon-cyan"
              >
                <User className="h-4 w-4" /> Register
              </Link>
            </>
          )}
          <Link
            to="/search"
            onClick={closeMenu}
            className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-foreground/80 hover:text-neon-cyan"
          >
            <Search className="h-4 w-4" /> Search
          </Link>
        </div>
        <UtcClock className="absolute bottom-6 left-6" />
      </Drawer>
    </>
  )
}
