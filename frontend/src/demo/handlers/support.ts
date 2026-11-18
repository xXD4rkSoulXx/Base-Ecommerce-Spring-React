import { HttpResponse } from 'msw'
import type { PageResponse } from '@/types/page'

const READ_ONLY_MESSAGE = 'This is a read-only demo. Changes are disabled.'

const DEFAULT_PAGE_SIZE = 10
const MAX_PAGE_SIZE = 100

export const paginate = <T>(items: T[], url: URL): PageResponse<T> => {
  const page = Math.max(Number(url.searchParams.get('page')) || 0, 0)
  const size = Math.min(Math.max(Number(url.searchParams.get('size')) || DEFAULT_PAGE_SIZE, 1), MAX_PAGE_SIZE)
  const totalElements = items.length
  const totalPages = Math.ceil(totalElements / size)

  return {
    content: items.slice(page * size, (page + 1) * size),
    page,
    size,
    totalElements,
    totalPages,
    first: page === 0,
    last: page >= totalPages - 1
  }
}

export const matchesQuery = (query: string | null, ...fields: Array<string | number | undefined | null>) => {
  const normalized = query?.trim().toLowerCase()

  if (!normalized) {
    return true
  }

  return fields.some((field) => field !== undefined && field !== null && String(field).toLowerCase().includes(normalized))
}

export const readId = (value: string | readonly string[] | undefined) => Number(Array.isArray(value) ? value[0] : value)

export const readBoolean = (url: URL, name: string) => {
  const value = url.searchParams.get(name)

  return value === null || value === '' ? undefined : value === 'true'
}

export const notFound = () => HttpResponse.json({ message: 'Not found' }, { status: 404 })

export const readOnly = () => HttpResponse.json({ message: READ_ONLY_MESSAGE }, { status: 403 })
