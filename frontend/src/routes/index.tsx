import { createBrowserRouter, redirect } from 'react-router-dom'
import { lazyRoute } from '@/routes/lazyRoute'
import { isDemoSession } from '@/demo/demoSession'
import { Layout } from '@/layouts/Layout'
import { ProtectedRoute } from '@/layouts/ProtectedRoute'
import { PublicRoute } from '@/layouts/PublicRoute'
import { ErrorPage } from '@/pages/ErrorPage'
import { Home } from '@/pages/Home'
import { Loading } from '@/pages/Loading'
import { NoPermission } from '@/pages/NoPermission'
import { NotFound } from '@/pages/NotFound'

export const router = createBrowserRouter([
  {
    loader: () => (isDemoSession() ? redirect('/admin') : null),
    element: <Layout />,
    errorElement: <ErrorPage />,
    hydrateFallbackElement: <Loading />,
    children: [
      { path: '/', element: <Home />, handle: { title: 'Urban Technical Streetwear' } },
      { path: '/shop', lazy: lazyRoute(() => import('@/pages/Shop'), 'Shop'), handle: { title: 'Shop' } },
      { path: '/product/:id', lazy: lazyRoute(() => import('@/pages/ProductDetails'), 'ProductDetails') },
      { path: '/search', lazy: lazyRoute(() => import('@/pages/Search'), 'Search'), handle: { title: 'Search' } },
      { path: '/about', lazy: lazyRoute(() => import('@/pages/About'), 'About'), handle: { title: 'About' } },
      {
        path: '/returns',
        lazy: lazyRoute(() => import('@/pages/ReturnPolicy'), 'ReturnPolicy'),
        handle: { title: 'Return Policy' }
      },
      { path: '/faq', lazy: lazyRoute(() => import('@/pages/Faq'), 'Faq'), handle: { title: 'FAQ' } },
      {
        path: '/terms',
        lazy: lazyRoute(() => import('@/pages/TermsAndConditions'), 'TermsAndConditions'),
        handle: { title: 'Terms and Conditions' }
      },
      {
        path: '/privacy',
        lazy: lazyRoute(() => import('@/pages/PrivacyPolicy'), 'PrivacyPolicy'),
        handle: { title: 'Privacy Policy' }
      },
      {
        path: '/wishlist',
        lazy: lazyRoute(() => import('@/pages/Wishlist'), 'Wishlist'),
        handle: { title: 'Wishlist' }
      },
      { path: '/cart', lazy: lazyRoute(() => import('@/pages/Cart'), 'Cart'), handle: { title: 'Cart' } },
      {
        element: <PublicRoute />,
        children: [
          {
            path: '/register',
            lazy: lazyRoute(() => import('@/pages/Register'), 'Register'),
            handle: { title: 'Register' }
          },
          { path: '/login', lazy: lazyRoute(() => import('@/pages/Login'), 'Login'), handle: { title: 'Login' } }
        ]
      },
      {
        element: <ProtectedRoute />,
        children: [
          {
            path: '/account',
            lazy: lazyRoute(() => import('@/pages/Account'), 'Account'),
            handle: { title: 'Account' }
          },
          {
            path: '/checkout',
            lazy: lazyRoute(() => import('@/pages/Checkout'), 'Checkout'),
            handle: { title: 'Checkout' }
          },
          {
            path: '/checkout/success',
            lazy: lazyRoute(() => import('@/pages/CheckoutSuccess'), 'CheckoutSuccess'),
            handle: { title: 'Order Confirmed' }
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
          {
            index: true,
            lazy: lazyRoute(() => import('@/pages/admin/AdminDashboard'), 'AdminDashboard'),
            handle: { title: 'Admin Dashboard' }
          },
          {
            element: <ProtectedRoute permission="PRODUCTS_WRITE" />,
            children: [
              {
                path: 'products/new',
                lazy: lazyRoute(() => import('@/pages/admin/AdminProductCreate'), 'AdminProductCreate'),
                handle: { title: 'Admin New Product' }
              }
            ]
          },
          {
            element: <ProtectedRoute permission="PRODUCTS_READ" />,
            children: [
              {
                path: 'products',
                lazy: lazyRoute(() => import('@/pages/admin/AdminProducts'), 'AdminProducts'),
                handle: { title: 'Admin Products' }
              },
              {
                path: 'products/:id',
                lazy: lazyRoute(() => import('@/pages/admin/AdminProductEdit'), 'AdminProductEdit'),
                handle: { title: 'Admin Edit Product' }
              }
            ]
          },
          {
            element: <ProtectedRoute permission="STOCK_READ" />,
            children: [
              {
                path: 'stock',
                lazy: lazyRoute(() => import('@/pages/admin/AdminStock'), 'AdminStock'),
                handle: { title: 'Admin Stock' }
              }
            ]
          },
          {
            element: <ProtectedRoute permission="ORDERS_READ" />,
            children: [
              {
                path: 'orders',
                lazy: lazyRoute(() => import('@/pages/admin/AdminOrders'), 'AdminOrders'),
                handle: { title: 'Admin Orders' }
              },
              {
                path: 'orders/:id',
                lazy: lazyRoute(() => import('@/pages/admin/AdminOrderDetails'), 'AdminOrderDetails'),
                handle: { title: 'Admin Order' }
              }
            ]
          },
          {
            element: <ProtectedRoute permission="RETURNS_READ" />,
            children: [
              {
                path: 'returns',
                lazy: lazyRoute(() => import('@/pages/admin/AdminReturns'), 'AdminReturns'),
                handle: { title: 'Admin Returns' }
              }
            ]
          },
          {
            element: <ProtectedRoute permission="CUSTOMERS_READ" />,
            children: [
              {
                path: 'customers',
                lazy: lazyRoute(() => import('@/pages/admin/AdminCustomers'), 'AdminCustomers'),
                handle: { title: 'Admin Customers' }
              },
              {
                path: 'customers/:id',
                lazy: lazyRoute(() => import('@/pages/admin/AdminCustomerDetails'), 'AdminCustomerDetails'),
                handle: { title: 'Admin Customer' }
              }
            ]
          },
          {
            element: <ProtectedRoute permission="REVIEWS_READ" />,
            children: [
              {
                path: 'reviews',
                lazy: lazyRoute(() => import('@/pages/admin/AdminReviews'), 'AdminReviews'),
                handle: { title: 'Admin Reviews' }
              }
            ]
          },
          {
            element: <ProtectedRoute permission="DISCOUNTS_READ" />,
            children: [
              {
                path: 'discounts',
                lazy: lazyRoute(() => import('@/pages/admin/AdminDiscounts'), 'AdminDiscounts'),
                handle: { title: 'Admin Discounts' }
              }
            ]
          },
          {
            element: <ProtectedRoute permission="TEAM_READ" />,
            children: [
              {
                path: 'team',
                lazy: lazyRoute(() => import('@/pages/admin/AdminTeam'), 'AdminTeam'),
                handle: { title: 'Admin Team' }
              }
            ]
          },
          {
            element: <ProtectedRoute permission="AUDIT_READ" />,
            children: [
              {
                path: 'audit',
                lazy: lazyRoute(() => import('@/pages/admin/AdminAuditLog'), 'AdminAuditLog'),
                handle: { title: 'Admin Audit Log' }
              }
            ]
          }
        ]
      }
    ]
  },
  { path: '/no-permission', element: <NoPermission /> },
  { path: '*', element: <NotFound /> }
])
