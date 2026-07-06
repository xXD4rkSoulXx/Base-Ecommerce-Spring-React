import { Outlet } from "react-router-dom"

import NavBar from "@/components/layout/NavBar"
import Ticker from "@/components/layout/Ticker"
import Footer from "@/components/layout/Footer"

function Layout() {
  return (
    <div className="min-h-dvh bg-background text-foreground">
      <NavBar />
      <Ticker />
      <main>
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}

export default Layout
