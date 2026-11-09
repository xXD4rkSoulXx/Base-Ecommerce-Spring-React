import { http, type HttpHandler } from 'msw'
import { readOnly } from '@/demo/handlers/support'

export const fallbackHandlers: HttpHandler[] = [http.all('*/api/*', readOnly)]
