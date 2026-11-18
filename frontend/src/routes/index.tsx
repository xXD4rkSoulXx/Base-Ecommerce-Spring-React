import { createBrowserRouter, redirect } from 'react-router-dom'
import { isDemoSession } from '@/demo/demoSession'
import { Layout } from '@/layouts/Layout'
import { ProtectedRoute } from '@/layouts/ProtectedRoute'
import { PublicRoute } from '@/layouts/PublicRoute'
import { ErrorPage } from '@/pages/ErrorPage'
import { Home } from '@/pages/Home'
import { Loading } from '@/pages/Loading'
import { NoPermission } from '@/pages/NoPermission'
import { NotFound } from '@/pages/NotFound'
import { lazyRoute } from '@/routes/lazyRoute'

export const router = createBrowserRouter([
  {
    loader: () => (isDemoSession() ? redirect('/admin') : null),
    element: <Layout />,
    errorElement: <ErrorPage />,
    hydrateFallbackElement: <Loading />,
    children: [
      { path: '/', element: <Home /> },
      { path: '/shop', lazy: lazyRoute(() => import('@/pages/Shop'), 'Shop') },
      { path: '/product/:id', lazy: lazyRoute(() => import('@/pages/ProductDetails'), 'ProductDetails') },
      { path: '/search', lazy: lazyRoute(() => import('@/pages/Search'), 'Search') },
      { path: '/about', lazy: lazyRoute(() => import('@/pages/About'), 'About') },
      { path: '/returns', lazy: lazyRoute(() => import('@/pages/ReturnPolicy'), 'ReturnPolicy') },
      { path: '/faq', lazy: lazyRoute(() => import('@/pages/Faq'), 'Faq') },
      { path: '/terms', lazy: lazyRoute(() => import('@/pages/TermsAndConditions'), 'TermsAndConditions') },
      { path: '/privacy', lazy: lazyRoute(() => import('@/pages/PrivacyPolicy'), 'PrivacyPolicy') },
      { path: '/wishlist', lazy: lazyRoute(() => import('@/pages/Wishlist'), 'Wishlist') },
      { path: '/cart', lazy: lazyRoute(() => import('@/pages/Cart'), 'Cart') },
      {
        element: <PublicRoute />,
        children: [
          { path: '/register', lazy: lazyRoute(() => import('@/pages/Register'), 'Register') },
          { path: '/login', lazy: lazyRoute(() => import('@/pages/Login'), 'Login') }
        ]
      },
      {
        element: <ProtectedRoute />,
        children: [
          { path: '/account', lazy: lazyRoute(() => import('@/pages/Account'), 'Account') },
          { path: '/checkout', lazy: lazyRoute(() => import('@/pages/Checkout'), 'Checkout') },
          {
            path: '/checkout/success',
            lazy: lazyRoute(() => import('@/pages/CheckoutSuccess'), 'CheckoutSuccess')
          }
        ]
      }
    ]
  },
  {
    element: <ProtectedRoute permission="DASHBOARD_READ" />,
    errorElement: <ErrorPage />,
    hydrateFallbackElement: <Loading />,
    children: [
      {
        path: '/admin',
        lazy: lazyRoute(() => import('@/layouts/AdminLayout'), 'AdminLayout'),
        children: [
          { index: true, lazy: lazyRoute(() => import('@/pages/admin/AdminDashboard'), 'AdminDashboard') },
          {
            element: <ProtectedRoute permission="PRODUCTS_WRITE" />,
            children: [
              {
                path: 'products/new',
                lazy: lazyRoute(() => import('@/pages/admin/AdminProductCreate'), 'AdminProductCreate')
              }
            ]
          },
          {
            element: <ProtectedRoute permission="PRODUCTS_READ" />,
            children: [
              { path: 'products', lazy: lazyRoute(() => import('@/pages/admin/AdminProducts'), 'AdminProducts') },
              {
                path: 'products/:id',
                lazy: lazyRoute(() => import('@/pages/admin/AdminProductEdit'), 'AdminProductEdit')
              }
            ]
          },
          {
            element: <ProtectedRoute permission="STOCK_READ" />,
            children: [{ path: 'stock', lazy: lazyRoute(() => import('@/pages/admin/AdminStock'), 'AdminStock') }]
          },
          {
            element: <ProtectedRoute permission="ORDERS_READ" />,
            children: [
              { path: 'orders', lazy: lazyRoute(() => import('@/pages/admin/AdminOrders'), 'AdminOrders') },
              {
                path: 'orders/:id',
                lazy: lazyRoute(() => import('@/pages/admin/AdminOrderDetails'), 'AdminOrderDetails')
              }
            ]
          },
          {
            element: <ProtectedRoute permission="RETURNS_READ" />,
            children: [{ path: 'returns', lazy: lazyRoute(() => import('@/pages/admin/AdminReturns'), 'AdminReturns') }]
          },
          {
            element: <ProtectedRoute permission="CUSTOMERS_READ" />,
            children: [
              { path: 'customers', lazy: lazyRoute(() => import('@/pages/admin/AdminCustomers'), 'AdminCustomers') },
              {
                path: 'customers/:id',
                lazy: lazyRoute(() => import('@/pages/admin/AdminCustomerDetails'), 'AdminCustomerDetails')
              }
            ]
          },
          {
            element: <ProtectedRoute permission="REVIEWS_READ" />,
            children: [{ path: 'reviews', lazy: lazyRoute(() => import('@/pages/admin/AdminReviews'), 'AdminReviews') }]
          },
          {
            element: <ProtectedRoute permission="DISCOUNTS_READ" />,
            children: [
              { path: 'discounts', lazy: lazyRoute(() => import('@/pages/admin/AdminDiscounts'), 'AdminDiscounts') }
            ]
          },
          {
            element: <ProtectedRoute permission="TEAM_READ" />,
            children: [{ path: 'team', lazy: lazyRoute(() => import('@/pages/admin/AdminTeam'), 'AdminTeam') }]
          },
          {
            element: <ProtectedRoute permission="AUDIT_READ" />,
            children: [{ path: 'audit', lazy: lazyRoute(() => import('@/pages/admin/AdminAuditLog'), 'AdminAuditLog') }]
          }
        ]
      }
    ]
  },
  { path: '/no-permission', element: <NoPermission /> },
  { path: '*', element: <NotFound /> }
])
