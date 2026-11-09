import type { HttpHandler } from 'msw'
import { catalogHandlers } from '@/demo/handlers/catalogHandlers'
import { communityHandlers } from '@/demo/handlers/communityHandlers'
import { dashboardHandlers } from '@/demo/handlers/dashboardHandlers'
import { fallbackHandlers } from '@/demo/handlers/fallbackHandlers'
import { salesHandlers } from '@/demo/handlers/salesHandlers'

export const handlers: HttpHandler[] = [
  ...dashboardHandlers,
  ...catalogHandlers,
  ...salesHandlers,
  ...communityHandlers,
  ...fallbackHandlers
]
