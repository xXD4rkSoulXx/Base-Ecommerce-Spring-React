import { createBrowserRouter, redirect } from 'react-router-dom'
import { isDemoSession } from '@/demo/demoSession'
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
import { CheckoutSuccess } from '@/pages/CheckoutSuccess'
import { AdminDashboard } from '@/pages/admin/AdminDashboard'
import { AdminProductCreate } from '@/pages/admin/AdminProductCreate'
import { AdminProducts } from '@/pages/admin/AdminProducts'
import { AdminProductEdit } from '@/pages/admin/AdminProductEdit'
import { AdminStock } from '@/pages/admin/AdminStock'
import { AdminOrders } from '@/pages/admin/AdminOrders'
import { AdminOrderDetails } from '@/pages/admin/AdminOrderDetails'
import { AdminReturns } from '@/pages/admin/AdminReturns'
import { AdminCustomers } from '@/pages/admin/AdminCustomers'
import { AdminCustomerDetails } from '@/pages/admin/AdminCustomerDetails'
import { AdminReviews } from '@/pages/admin/AdminReviews'
import { AdminDiscounts } from '@/pages/admin/AdminDiscounts'
import { AdminTeam } from '@/pages/admin/AdminTeam'
import { AdminAuditLog } from '@/pages/admin/AdminAuditLog'
import { NoPermission } from '@/pages/NoPermission'
import { NotFound } from '@/pages/NotFound'

export const router = createBrowserRouter([
  {
    loader: () => (isDemoSession() ? redirect('/admin') : null),
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
    element: <ProtectedRoute permission="DASHBOARD_READ" />,
    children: [
      {
        path: '/admin',
        element: <AdminLayout />,
        children: [
          { index: true, element: <AdminDashboard /> },
          {
            element: <ProtectedRoute permission="PRODUCTS_WRITE" />,
            children: [{ path: 'products/new', element: <AdminProductCreate /> }]
          },
          {
            element: <ProtectedRoute permission="PRODUCTS_READ" />,
            children: [
              { path: 'products', element: <AdminProducts /> },
              { path: 'products/:id', element: <AdminProductEdit /> }
            ]
          },
          {
            element: <ProtectedRoute permission="STOCK_READ" />,
            children: [{ path: 'stock', element: <AdminStock /> }]
          },
          {
            element: <ProtectedRoute permission="ORDERS_READ" />,
            children: [
              { path: 'orders', element: <AdminOrders /> },
              { path: 'orders/:id', element: <AdminOrderDetails /> }
            ]
          },
          {
            element: <ProtectedRoute permission="RETURNS_READ" />,
            children: [{ path: 'returns', element: <AdminReturns /> }]
          },
          {
            element: <ProtectedRoute permission="CUSTOMERS_READ" />,
            children: [
              { path: 'customers', element: <AdminCustomers /> },
              { path: 'customers/:id', element: <AdminCustomerDetails /> }
            ]
          },
          {
            element: <ProtectedRoute permission="REVIEWS_READ" />,
            children: [{ path: 'reviews', element: <AdminReviews /> }]
          },
          {
            element: <ProtectedRoute permission="DISCOUNTS_READ" />,
            children: [{ path: 'discounts', element: <AdminDiscounts /> }]
          },
          {
            element: <ProtectedRoute permission="TEAM_READ" />,
            children: [{ path: 'team', element: <AdminTeam /> }]
          },
          {
            element: <ProtectedRoute permission="AUDIT_READ" />,
            children: [{ path: 'audit', element: <AdminAuditLog /> }]
          }
        ]
      }
    ]
  },
  { path: '/no-permission', element: <NoPermission /> },
  { path: '*', element: <NotFound /> }
])
