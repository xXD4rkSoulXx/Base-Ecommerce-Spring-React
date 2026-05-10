import { Outlet } from "react-router-dom";
import { Navbar } from "../components/layout/NavBar";
import { Ticker } from "../components/layout/Ticker";
import { Footer } from "../components/layout/Footer";

export default function Layout() {
  return (
    <>
      <Navbar />
      <Ticker />
      <main>
        <Outlet />
      </main>
      <Footer />
    </>
  );
}