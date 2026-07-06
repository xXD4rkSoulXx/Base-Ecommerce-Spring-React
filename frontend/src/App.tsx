import { Route, Routes } from "react-router-dom"

import Home from "@/pages/Home"
import Layout from "@/layouts/Layout"
import NotFound from "@/pages/NotFound"
import About from "@/pages/About"
import Search from "@/pages/Search"

function App() {
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

export default App
