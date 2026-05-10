import { Link, NavLink, useNavigate } from "react-router-dom";
import { Menu, Search, ShoppingBag, User, X } from "lucide-react";
import { useCart } from "@/store/cartStore";
import { useEffect, useMemo, useState } from "react";

export const Navbar = () => {
  const count = useCart((s) => s.count());
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const [now, setNow] = useState(new Date());
  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(t);
  }, []);
  const time = useMemo(
    () => now.toLocaleTimeString("pt-PT", { hour: "2-digit", minute: "2-digit", second: "2-digit" }),
    [now]
  );

  const navItems = [
    { to: "/shop", label: "Shop" },
    { to: "/about", label: "About" },
    { to: "/admin", label: "Admin" },
  ];

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
            <Link to="/" className="font-display text-2xl italic tracking-tighter">
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
            <span className="hidden font-mono text-[10px] uppercase tracking-widest text-muted-foreground md:inline">
              UTC {time}
            </span>
            <button
              onClick={() => navigate("/search")}
              className="text-foreground/70 transition-colors hover:text-neon-cyan"
              aria-label="Search"
            >
              <Search className="h-4 w-4" />
            </button>
            <button
              onClick={() => navigate("/account")}
              className="text-foreground/70 transition-colors hover:text-neon-cyan"
              aria-label="Account"
            >
              <User className="h-4 w-4" />
            </button>
            <button
              onClick={() => navigate("/cart")}
              className="group flex items-center gap-2 border border-neon-cyan px-3 py-1.5 text-[10px] font-bold uppercase tracking-[0.2em] text-neon-cyan transition-all hover:bg-neon-cyan hover:text-primary-foreground"
            >
              <ShoppingBag className="h-3 w-3" />
              <span>Cart [{String(count).padStart(2, "0")}]</span>
            </button>
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
          <div className="mt-8 flex flex-col gap-3">
            <button
              onClick={() => {
                setOpen(false);
                navigate("/account");
              }}
              className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-foreground/80 hover:text-neon-cyan"
            >
              <User className="h-4 w-4" /> Account
            </button>
            <button
              onClick={() => {
                setOpen(false);
                navigate("/search");
              }}
              className="flex items-center gap-3 text-xs font-bold uppercase tracking-[0.2em] text-foreground/80 hover:text-neon-cyan"
            >
              <Search className="h-4 w-4" /> Search
            </button>
          </div>
          <p className="absolute bottom-6 left-6 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            UTC {time}
          </p>
        </aside>
      </div>
    </>
  );
};
