import { Route, Routes } from "react-router-dom";
import Layout from "./layouts/Layout";
import Home from "./pages/Home";
import NotFound from "./pages/NotFound";
import About from "./pages/About";
import Search from "./pages/Search";

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/search" element={<Search />} />
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
