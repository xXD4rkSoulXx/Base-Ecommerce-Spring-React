import { setupWorker } from 'msw/browser'
import { handlers } from '@/demo/handlers'

export const worker = setupWorker(...handlers)
