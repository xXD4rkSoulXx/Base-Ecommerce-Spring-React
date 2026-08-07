import { Route, Routes } from 'react-router-dom'

import Layout from '@/layouts/Layout'
import Home from '@/pages/Home'
import Shop from '@/pages/Shop'
import Search from '@/pages/Search'
import About from '@/pages/About'
import PublicRoute from '@/layouts/PublicRoute'
import Register from '@/pages/Register'
import Login from '@/pages/Login'
import ProtectedRoute from '@/layouts/ProtectedRoute'
import Account from '@/pages/Account'
import AdminLayout from '@/layouts/AdminLayout'
import AdminDashboard from '@/pages/admin/AdminDashboard'
import AdminProducts from '@/pages/admin/AdminProducts'
import AdminDiscounts from '@/pages/admin/AdminDiscounts'
import AdminOrders from '@/pages/admin/AdminOrders'
import AdminUsers from '@/pages/admin/AdminUsers'
import NoPermission from '@/pages/NoPermission'
import NotFound from '@/pages/NotFound'

function AppRouter() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/shop" element={<Shop />} />
        <Route path="/search" element={<Search />} />
        <Route path="/about" element={<About />} />
        <Route element={<PublicRoute />}>
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
        </Route>
        <Route element={<ProtectedRoute />}>
          <Route path="/account" element={<Account />} />
        </Route>
      </Route>
      <Route path="/admin" element={<AdminLayout />}>
        <Route element={<ProtectedRoute allowedRoles={['OPERATOR', 'MANAGER', 'ADMIN']} />}>
          <Route index element={<AdminDashboard />} />
          <Route path="products" element={<AdminProducts />} />
          <Route path="orders" element={<AdminOrders />} />
        </Route>
        <Route element={<ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']} />}>
          <Route path="discounts" element={<AdminDiscounts />} />
        </Route>
        <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
          <Route path="users" element={<AdminUsers />} />
        </Route>
      </Route>
      <Route path="/no-permission" element={<NoPermission />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default AppRouter
