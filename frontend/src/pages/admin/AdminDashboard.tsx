import { lazy, Suspense } from 'react'
import { AdminDashboardKpiCard } from '@/components/card/admin/AdminDashboardKpiCard'
import { AdminDashboardTodoCard } from '@/components/card/admin/AdminDashboardTodoCard'
import { AdminDashboardPeriodFilter } from '@/components/filter/admin/AdminDashboardPeriodFilter'
import { AdminDashboardActivitySection } from '@/components/section/admin/AdminDashboardActivitySection'
import { AdminDashboardDiscountCodesSection } from '@/components/section/admin/AdminDashboardDiscountCodesSection'
import { AdminDashboardLowStockSection } from '@/components/section/admin/AdminDashboardLowStockSection'
import { AdminDashboardRecentOrdersSection } from '@/components/section/admin/AdminDashboardRecentOrdersSection'
import { AdminDashboardReturnsSection } from '@/components/section/admin/AdminDashboardReturnsSection'
import { AdminDashboardSection } from '@/components/section/admin/AdminDashboardSection'
import { AdminDashboardTopProductsSection } from '@/components/section/admin/AdminDashboardTopProductsSection'
import { ORDER_STATUS_LABELS } from '@/constants'
import { useAdminDashboardDays } from '@/hooks/admin/useAdminDashboardDays'
import { useAdminDashboardQuery } from '@/hooks/admin/useAdminDashboardQuery'
import { usePermission } from '@/hooks/usePermission'
import { formatCategory, formatNumber, formatPrice, formatRating } from '@/utils/format'

const AdminDashboardSalesChart = lazy(() =>
  import('@/components/chart/admin/AdminDashboardSalesChart').then((module) => ({
    default: module.AdminDashboardSalesChart
  }))
)

const AdminDashboardBarChart = lazy(() =>
  import('@/components/chart/admin/AdminDashboardBarChart').then((module) => ({
    default: module.AdminDashboardBarChart
  }))
)

const chartFallback = (
  <p className="p-4 font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
)

export const AdminDashboard = () => {
  const days = useAdminDashboardDays()
  const canViewOrders = usePermission('ORDERS_READ')
  const canViewReturns = usePermission('RETURNS_READ')
  const canViewStock = usePermission('STOCK_READ')
  const canViewProducts = usePermission('PRODUCTS_READ')
  const canViewDiscounts = usePermission('DISCOUNTS_READ')
  const canViewCustomers = usePermission('CUSTOMERS_READ')
  const canViewReviews = usePermission('REVIEWS_READ')
  const canViewAudit = usePermission('AUDIT_READ')

  const summary = useAdminDashboardQuery('summary', canViewOrders, days)
  const sales = useAdminDashboardQuery('sales', canViewOrders, days)
  const ordersByStatus = useAdminDashboardQuery('orders-by-status', canViewOrders, days)
  const salesByCategory = useAdminDashboardQuery('sales-by-category', canViewOrders, days)
  const recentOrders = useAdminDashboardQuery('recent-orders', canViewOrders)
  const pendingOrders = useAdminDashboardQuery('pending-orders', canViewOrders)
  const pendingReturns = useAdminDashboardQuery('pending-returns', canViewReturns)
  const lowStock = useAdminDashboardQuery('low-stock', canViewStock)
  const returnsToHandle = useAdminDashboardQuery('returns-to-handle', canViewReturns)
  const lowestStock = useAdminDashboardQuery('lowest-stock', canViewStock)
  const topProducts = useAdminDashboardQuery('top-products', canViewProducts, days)
  const topDiscountCodes = useAdminDashboardQuery('top-discount-codes', canViewDiscounts, days)
  const customers = useAdminDashboardQuery('customers', canViewCustomers, days)
  const reviews = useAdminDashboardQuery('reviews', canViewReviews, days)
  const recentActivity = useAdminDashboardQuery('recent-activity', canViewAudit)

  const canViewTodo = canViewOrders || canViewReturns || canViewStock
  const statusData = (ordersByStatus.data ?? []).map((item) => ({
    label: ORDER_STATUS_LABELS[item.status],
    value: item.count
  }))
  const categoryData = (salesByCategory.data ?? []).map((item) => ({
    label: formatCategory(item.category),
    value: item.unitsSold
  }))

  return (
    <div className="space-y-8 p-6 md:p-10">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">Dashboard</h1>
        <AdminDashboardPeriodFilter />
      </div>
      {canViewTodo && (
        <section className="grid gap-4 sm:grid-cols-3">
          {canViewOrders && (
            <AdminDashboardTodoCard
              label="Orders to handle"
              count={pendingOrders.data?.count}
              to="/admin/orders?status=TODO"
              isError={pendingOrders.isError}
            />
          )}
          {canViewReturns && (
            <AdminDashboardTodoCard
              label="Pending returns"
              count={pendingReturns.data?.count}
              to="/admin/returns?status=TODO"
              isError={pendingReturns.isError}
            />
          )}
          {canViewStock && (
            <AdminDashboardTodoCard
              label="Low stock variants"
              count={lowStock.data?.count}
              to="/admin/stock?lowStock=true"
              isError={lowStock.isError}
            />
          )}
        </section>
      )}
      {(canViewOrders || canViewCustomers || canViewReviews) && (
        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {canViewOrders && (
            <>
              <AdminDashboardKpiCard
                label="Net revenue"
                value={summary.data?.netRevenue}
                previous={summary.data?.previousNetRevenue}
                format={formatPrice}
              />
              <AdminDashboardKpiCard
                label="Refunded"
                value={summary.data?.refunded}
                previous={summary.data?.previousRefunded}
                format={formatPrice}
                higherIsBetter={false}
              />
              <AdminDashboardKpiCard
                label="Orders"
                value={summary.data?.orders}
                previous={summary.data?.previousOrders}
                format={formatNumber}
              />
              <AdminDashboardKpiCard
                label="Average order"
                value={summary.data?.averageOrderValue}
                previous={summary.data?.previousAverageOrderValue}
                format={formatPrice}
              />
            </>
          )}
          {canViewCustomers && (
            <AdminDashboardKpiCard
              label="New customers"
              value={customers.data?.newCustomers}
              previous={customers.data?.previousNewCustomers}
              format={formatNumber}
            />
          )}
          {canViewReviews && (
            <>
              <AdminDashboardKpiCard
                label="New reviews"
                value={reviews.data?.newReviews}
                previous={reviews.data?.previousNewReviews}
                format={formatNumber}
              />
              <AdminDashboardKpiCard
                label="Average rating"
                value={reviews.data?.averageRating ?? undefined}
                previous={reviews.data?.previousAverageRating ?? undefined}
                format={formatRating}
              />
            </>
          )}
        </section>
      )}
      {canViewOrders && (
        <section className="space-y-6">
          <AdminDashboardSection title="Sales and refunds per day" isError={sales.isError}>
            {sales.data && (
              <div className="p-4">
                <Suspense fallback={chartFallback}>
                  <AdminDashboardSalesChart data={sales.data} />
                </Suspense>
              </div>
            )}
          </AdminDashboardSection>
          <div className="grid gap-6 lg:grid-cols-2">
            <AdminDashboardSection title="Orders by status" isError={ordersByStatus.isError}>
              {ordersByStatus.data && (
                <Suspense fallback={chartFallback}>
                  <AdminDashboardBarChart data={statusData} name="Orders" color="var(--color-neon-cyan)" />
                </Suspense>
              )}
            </AdminDashboardSection>
            <AdminDashboardSection
              title="Units sold by category"
              isError={salesByCategory.isError}
              isEmpty={salesByCategory.data?.length === 0}
              emptyMessage="No sales in this period"
            >
              {salesByCategory.data && (
                <Suspense fallback={chartFallback}>
                  <AdminDashboardBarChart data={categoryData} name="Units" color="var(--color-neon-pink)" />
                </Suspense>
              )}
            </AdminDashboardSection>
          </div>
        </section>
      )}
      <section className="grid gap-6 lg:grid-cols-2">
        {canViewOrders && (
          <AdminDashboardSection
            title="Latest orders"
            isError={recentOrders.isError}
            isEmpty={recentOrders.data?.length === 0}
            emptyMessage="No orders yet"
          >
            {recentOrders.data && <AdminDashboardRecentOrdersSection orders={recentOrders.data} />}
          </AdminDashboardSection>
        )}
        {canViewReturns && (
          <AdminDashboardSection
            title="Returns to handle"
            isError={returnsToHandle.isError}
            isEmpty={returnsToHandle.data?.length === 0}
            emptyMessage="Nothing waiting for action"
          >
            {returnsToHandle.data && <AdminDashboardReturnsSection returns={returnsToHandle.data} />}
          </AdminDashboardSection>
        )}
        {canViewStock && (
          <AdminDashboardSection
            title="Lowest stock"
            isError={lowestStock.isError}
            isEmpty={lowestStock.data?.length === 0}
            emptyMessage="No low stock variants"
          >
            {lowestStock.data && <AdminDashboardLowStockSection items={lowestStock.data} />}
          </AdminDashboardSection>
        )}
        {canViewProducts && (
          <AdminDashboardSection
            title="Top products (units sold)"
            isError={topProducts.isError}
            isEmpty={topProducts.data?.length === 0}
            emptyMessage="No sales in this period"
          >
            {topProducts.data && <AdminDashboardTopProductsSection products={topProducts.data} />}
          </AdminDashboardSection>
        )}
        {canViewDiscounts && (
          <AdminDashboardSection
            title="Most used discount codes"
            isError={topDiscountCodes.isError}
            isEmpty={topDiscountCodes.data?.length === 0}
            emptyMessage="No discount codes used in this period"
          >
            {topDiscountCodes.data && <AdminDashboardDiscountCodesSection codes={topDiscountCodes.data} />}
          </AdminDashboardSection>
        )}
        {canViewAudit && (
          <AdminDashboardSection
            title="Recent activity"
            isError={recentActivity.isError}
            isEmpty={recentActivity.data?.length === 0}
            emptyMessage="No activity yet"
          >
            {recentActivity.data && <AdminDashboardActivitySection activity={recentActivity.data} />}
          </AdminDashboardSection>
        )}
      </section>
    </div>
  )
}
