import { useEffect, useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { Heart, Menu, Search, ShoppingBag, User, X } from 'lucide-react'

import { useCartStore } from '@/store/useCartStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { hasAnyRole, useAuthStore } from '@/store/useAuthStore'

interface NavItem {
  to: string;
  label: string;
}

function NavBar() {
  const [open, setOpen] = useState(false);

  const user = useAuthStore((state) => state.user);
  const isElevatedOperator = hasAnyRole(user, ['OPERATOR', 'MANAGER', 'ADMIN']);

  const navItems: NavItem[] = [
    { to: "/shop", label: "Shop" },
    { to: "/about", label: "About" },
    isElevatedOperator && { to: "/admin", label: "Admin" }
  ].filter((item): item is NavItem => Boolean(item));

  useEffect(() => {
    document.body.style.overflow = open ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [open]);

  const [now, setNow] = useState(new Date());

  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(t);
  }, []);

  const time = now.toLocaleTimeString("pt-PT", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit"
  });

  const countCart = useCartStore((state) => state.getTotalItems());
  const countWishlist = useWishlistStore((state) => state.getTotalItems());

  return (
    <>
      <nav className="sticky top-0 z-40 border-b border-border bg-background/85 backdrop-blur-xl">
        <div className="mx-auto flex max-w-[1440px] items-center justify-between px-6 py-4 md:px-10">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setOpen(true)}
              className="text-foreground/80 transition-colors hover:text-neon-cyan xl:hidden"
              aria-label="Open menu"
            >
              <Menu className="h-5 w-5" />
            </button>
            <Link
              to="/"
              className="font-display text-2xl italic tracking-tighter"
            >
              VELO_CITI
            </Link>
          </div>
          <div className="hidden items-center gap-10 xl:flex">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `text-xs font-bold uppercase tracking-[0.2em] transition-colors ${
                    isActive ? "text-neon-cyan" : "text-foreground hover:text-neon-cyan"
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </div>
          <div className="flex items-center gap-4 md:gap-6">
            <span
              className="hidden font-mono text-[10px] uppercase tracking-widest text-muted-foreground md:inline"
            >
              UTC {time}
            </span>
            <Link
              to="/search"
              className="text-foreground/70 transition-colors hover:text-neon-cyan"
              aria-label="Search"
            >
              <Search className="h-4 w-4" />
            </Link>
            <Link
              to="/wishlist"
              className="relative text-foreground/70 transition-colors hover:text-neon-pink"
              aria-label="Wishlist"
            >
              <Heart className="h-4 w-4" />
              {countWishlist > 0 && (
                <span className="absolute -right-2 -top-2 flex h-4 min-w-4 items-center justify-center bg-neon-pink px-1 font-mono text-[9px] font-black text-primary-foreground">
                  {countWishlist}
                </span>
              )}
            </Link>
            {user ? (
              <Link
                to="/account"
                className="text-foreground/70 transition-colors hover:text-neon-cyan"
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
              className="group flex items-center gap-2 border border-neon-cyan px-3 py-1.5 text-[10px] font-bold uppercase tracking-[0.2em] text-neon-cyan transition-all hover:bg-neon-cyan hover:text-primary-foreground"
            >
              <ShoppingBag className="h-3 w-3" />
              <span>Cart [{String(countCart).padStart(2, "0")}]</span>
            </Link>
          </div>
        </div>
      </nav>
      <div
        className={`fixed inset-0 z-50 xl:hidden ${open ? "" : "pointer-events-none"}`}
        aria-hidden={!open}
      >
        <div
          onClick={() => setOpen(false)}
          className={`absolute inset-0 bg-background/80 backdrop-blur-sm transition-opacity ${
            open ? "opacity-100" : "opacity-0"
          }`}
          role="button"
          aria-label="Close menu"
        />
        <aside
          className={`absolute left-0 top-0 h-full w-[85%] max-w-sm border-r border-neon-cyan/30 bg-background p-6 shadow-2xl shadow-neon-cyan/10 transition-transform duration-300 ${
            open ? "translate-x-0" : "-translate-x-full"
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="font-display text-xl italic tracking-tighter">VELO_CITI</span>
            <button
              onClick={() => setOpen(false)}
              className="text-foreground/70 hover:text-neon-cyan"
              aria-label="Close menu"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
          <p className="mt-2 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
            Navigation
          </p>
          <nav className="mt-8 flex flex-col">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setOpen(false)}
                className={({ isActive }) =>
                  `border-b border-border py-4 text-sm font-bold uppercase tracking-[0.2em] transition-colors ${
                    isActive ? "text-neon-cyan" : "text-foreground hover:text-neon-cyan"
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
                className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-foreground/80 hover:text-neon-cyan"
              >
                <User className="h-4 w-4" /> Account
              </Link>
            ) : (
              <>
                <Link
                  to="/login"
                  className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-foreground/80 hover:text-neon-cyan"
                >
                  <User className="h-4 w-4" /> Login
                </Link>
                <Link
                  to="/register"
                  className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-neon-pink hover:text-neon-pink/80"
                >
                  <User className="h-4 w-4" /> Register
                </Link>
              </>
            )}
            <Link
              to="/search"
              onClick={() => setOpen(false)}
              className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-foreground/80 hover:text-neon-cyan"
            >
              <Search className="h-4 w-4" /> Search
            </Link>
          </div>
          <p className="absolute bottom-6 left-6 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            UTC {time}
          </p>
        </aside>
      </div>
    </>
  );
}

export default NavBar
