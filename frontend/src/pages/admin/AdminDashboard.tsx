import { lazy, Suspense } from 'react'
import { AdminDashboardPeriodFilter } from '@/components/section/admin/AdminDashboardPeriodFilter'
import { AdminDashboardActivity } from '@/components/ui/admin/AdminDashboardActivity'
import { AdminDashboardDiscountCodes } from '@/components/ui/admin/AdminDashboardDiscountCodes'
import { AdminDashboardKpi } from '@/components/ui/admin/AdminDashboardKpi'
import { AdminDashboardLowStockList } from '@/components/ui/admin/AdminDashboardLowStockList'
import { AdminDashboardPanel } from '@/components/ui/admin/AdminDashboardPanel'
import { AdminDashboardRecentOrders } from '@/components/ui/admin/AdminDashboardRecentOrders'
import { AdminDashboardReturnsList } from '@/components/ui/admin/AdminDashboardReturnsList'
import { AdminDashboardTodoCard } from '@/components/card/admin/AdminDashboardTodoCard'
import { AdminDashboardTopProducts } from '@/components/ui/admin/AdminDashboardTopProducts'
import { useAdminDashboardDays } from '@/hooks/admin/useAdminDashboardDays'
import { useAdminDashboardQuery } from '@/hooks/admin/useAdminDashboardQuery'
import { usePermission } from '@/hooks/usePermission'

const AdminDashboardSalesChart = lazy(() =>
  import('@/components/section/admin/AdminDashboardSalesChart').then((module) => ({
    default: module.AdminDashboardSalesChart
  }))
)

const AdminDashboardBarChart = lazy(() =>
  import('@/components/section/admin/AdminDashboardBarChart').then((module) => ({
    default: module.AdminDashboardBarChart
  }))
)

const currencyFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const numberFormatter = new Intl.NumberFormat('pt-PT')
const ratingFormatter = new Intl.NumberFormat('pt-PT', { maximumFractionDigits: 1 })

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
    label: item.status.replaceAll('_', ' '),
    value: item.count
  }))
  const categoryData = (salesByCategory.data ?? []).map((item) => ({
    label: item.category.replaceAll('_', ' '),
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
              <AdminDashboardKpi
                label="Net revenue"
                value={summary.data?.netRevenue}
                previous={summary.data?.previousNetRevenue}
                format={currencyFormatter.format}
              />
              <AdminDashboardKpi
                label="Refunded"
                value={summary.data?.refunded}
                previous={summary.data?.previousRefunded}
                format={currencyFormatter.format}
                higherIsBetter={false}
              />
              <AdminDashboardKpi
                label="Orders"
                value={summary.data?.orders}
                previous={summary.data?.previousOrders}
                format={numberFormatter.format}
              />
              <AdminDashboardKpi
                label="Average order"
                value={summary.data?.averageOrderValue}
                previous={summary.data?.previousAverageOrderValue}
                format={currencyFormatter.format}
              />
            </>
          )}
          {canViewCustomers && (
            <AdminDashboardKpi
              label="New customers"
              value={customers.data?.newCustomers}
              previous={customers.data?.previousNewCustomers}
              format={numberFormatter.format}
            />
          )}
          {canViewReviews && (
            <>
              <AdminDashboardKpi
                label="New reviews"
                value={reviews.data?.newReviews}
                previous={reviews.data?.previousNewReviews}
                format={numberFormatter.format}
              />
              <AdminDashboardKpi
                label="Average rating"
                value={reviews.data?.averageRating ?? undefined}
                previous={reviews.data?.previousAverageRating ?? undefined}
                format={(value) => `${ratingFormatter.format(value)} / 5`}
              />
            </>
          )}
        </section>
      )}

      {canViewOrders && (
        <section className="space-y-6">
          <AdminDashboardPanel
            title="Sales and refunds per day"
            isError={sales.isError}
            isEmpty={false}
            emptyMessage=""
          >
            {sales.data && (
              <div className="p-4">
                <Suspense fallback={chartFallback}>
                  <AdminDashboardSalesChart data={sales.data} />
                </Suspense>
              </div>
            )}
          </AdminDashboardPanel>

          <div className="grid gap-6 lg:grid-cols-2">
            <AdminDashboardPanel
              title="Orders by status"
              isError={ordersByStatus.isError}
              isEmpty={false}
              emptyMessage=""
            >
              {ordersByStatus.data && (
                <Suspense fallback={chartFallback}>
                  <AdminDashboardBarChart data={statusData} name="Orders" color="var(--color-neon-cyan)" />
                </Suspense>
              )}
            </AdminDashboardPanel>

            <AdminDashboardPanel
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
            </AdminDashboardPanel>
          </div>
        </section>
      )}

      <section className="grid gap-6 lg:grid-cols-2">
        {canViewOrders && (
          <AdminDashboardPanel
            title="Latest orders"
            isError={recentOrders.isError}
            isEmpty={recentOrders.data?.length === 0}
            emptyMessage="No orders yet"
          >
            {recentOrders.data && <AdminDashboardRecentOrders orders={recentOrders.data} />}
          </AdminDashboardPanel>
        )}
        {canViewReturns && (
          <AdminDashboardPanel
            title="Returns to handle"
            isError={returnsToHandle.isError}
            isEmpty={returnsToHandle.data?.length === 0}
            emptyMessage="Nothing waiting for action"
          >
            {returnsToHandle.data && <AdminDashboardReturnsList returns={returnsToHandle.data} />}
          </AdminDashboardPanel>
        )}
        {canViewStock && (
          <AdminDashboardPanel
            title="Lowest stock"
            isError={lowestStock.isError}
            isEmpty={lowestStock.data?.length === 0}
            emptyMessage="No low stock variants"
          >
            {lowestStock.data && <AdminDashboardLowStockList items={lowestStock.data} />}
          </AdminDashboardPanel>
        )}
        {canViewProducts && (
          <AdminDashboardPanel
            title="Top products (units sold)"
            isError={topProducts.isError}
            isEmpty={topProducts.data?.length === 0}
            emptyMessage="No sales in this period"
          >
            {topProducts.data && <AdminDashboardTopProducts products={topProducts.data} />}
          </AdminDashboardPanel>
        )}
        {canViewDiscounts && (
          <AdminDashboardPanel
            title="Most used discount codes"
            isError={topDiscountCodes.isError}
            isEmpty={topDiscountCodes.data?.length === 0}
            emptyMessage="No discount codes used in this period"
          >
            {topDiscountCodes.data && <AdminDashboardDiscountCodes codes={topDiscountCodes.data} />}
          </AdminDashboardPanel>
        )}
        {canViewAudit && (
          <AdminDashboardPanel
            title="Recent activity"
            isError={recentActivity.isError}
            isEmpty={recentActivity.data?.length === 0}
            emptyMessage="No activity yet"
          >
            {recentActivity.data && <AdminDashboardActivity activity={recentActivity.data} />}
          </AdminDashboardPanel>
        )}
      </section>
    </div>
  )
}
