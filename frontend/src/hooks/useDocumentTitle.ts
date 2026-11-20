import { useEffect } from 'react'
import { APP_NAME } from '@/constants'

export const useDocumentTitle = (title: string | undefined) => {
  useEffect(() => {
    if (title) {
      document.title = `${title} - ${APP_NAME}`
    }
  }, [title])
}
