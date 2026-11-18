import { AdminFilterCheckbox } from '@/components/filter/admin/AdminFilterCheckbox'
import { AdminFilterSelect, type FilterOption } from '@/components/filter/admin/AdminFilterSelect'
import { AdminFilterBar } from '@/components/filter/admin/AdminFilterBar'
import { formatCategory } from '@/utils/format'
import type { ProductCategory } from '@/types/product'

const CATEGORIES: ProductCategory[] = ['OUTERWEAR', 'TOPS', 'BOTTOMS', 'FOOTWEAR', 'UNDERWEAR', 'ACCESSORIES']

const CATEGORY_OPTIONS: FilterOption[] = [
  { value: '', label: 'All categories' },
  ...CATEGORIES.map((category) => ({ value: category, label: formatCategory(category) }))
]

export const AdminProductFilters = () => (
  <AdminFilterBar searchPlaceholder="Search by name or ref…">
    <AdminFilterSelect param="category" label="Category" options={CATEGORY_OPTIONS} />
    <AdminFilterCheckbox param="lowStock">Low stock</AdminFilterCheckbox>
  </AdminFilterBar>
)
