import { createBrowserRouter } from 'react-router-dom'
import { Layout } from '@/layouts/Layout'
import { AdminLayout } from '@/layouts/AdminLayout'
import { PublicRoute } from '@/layouts/PublicRoute'
import { ProtectedRoute } from '@/layouts/ProtectedRoute'
import { Home } from '@/pages/Home'
import { Shop } from '@/pages/Shop'
import { ProductDetails } from '@/pages/ProductDetails'
import { Search } from '@/pages/Search'
import { About } from '@/pages/About'
import { ReturnPolicy } from '@/pages/ReturnPolicy'
import { Faq } from '@/pages/Faq'
import { TermsAndConditions } from '@/pages/TermsAndConditions'
import { PrivacyPolicy } from '@/pages/PrivacyPolicy'
import { Wishlist } from '@/pages/Wishlist'
import { Cart } from '@/pages/Cart'
import { Register } from '@/pages/Register'
import { Login } from '@/pages/Login'
import { Account } from '@/pages/Account'
import { Checkout } from '@/pages/Checkout'
import { CheckoutSuccess } from '@/pages/CheckoutSucess'
import { AdminDashboard } from '@/pages/admin/AdminDashboard'
import { AdminProducts } from '@/pages/admin/AdminProducts'
import { AdminOrders } from '@/pages/admin/AdminOrders'
import { AdminDiscounts } from '@/pages/admin/AdminDiscounts'
import { AdminUsers } from '@/pages/admin/AdminUsers'
import { NoPermission } from '@/pages/NoPermission'
import { NotFound } from '@/pages/NotFound'

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { path: '/', element: <Home /> },
      { path: '/shop', element: <Shop /> },
      { path: '/product/:id', element: <ProductDetails /> },
      { path: '/search', element: <Search /> },
      { path: '/about', element: <About />},
      { path: '/returns', element: <ReturnPolicy /> },
      { path: '/faq', element: <Faq /> },
      { path: '/terms', element: <TermsAndConditions /> },
      { path: '/privacy', element: <PrivacyPolicy /> },
      { path: '/wishlist', element: <Wishlist /> },
      { path: '/cart', element: <Cart /> },
      {
        element: <PublicRoute />,
        children: [
          { path: '/register', element: <Register /> },
          { path: '/login', element: <Login /> }
        ]
      },
      {
        element: <ProtectedRoute />,
                children: [
          { path: '/account', element: <Account /> },
          { path: '/checkout', element: <Checkout /> },
          { path: '/checkout/success', element: <CheckoutSuccess /> }
        ]
      }
    ]
  },
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      {
        element: <ProtectedRoute allowedRoles={['OPERATOR', 'MANAGER', 'ADMIN']} />,
        children: [
          { index: true, element: <AdminDashboard /> },
          { path: 'products', element: <AdminProducts /> },
          { path: 'orders', element: <AdminOrders /> }
        ]
      },
      {
        element: <ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']} />,
        children: [
          { path: 'discounts', element: <AdminDiscounts /> }
        ]
      },
      {
        element: <ProtectedRoute allowedRoles={['ADMIN']} />,
        children: [
          { path: 'users', element: <AdminUsers /> }
        ]
      }
    ]
  },
  { path: '/no-permission', element: <NoPermission /> },
  { path: '*', element: <NotFound /> }
])
