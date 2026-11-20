import type { MouseEvent } from 'react'
import { MAIN_CONTENT_ID } from '@/constants'

export const SkipLink = () => {
  const skip = (event: MouseEvent<HTMLAnchorElement>) => {
    event.preventDefault()
    document.getElementById(MAIN_CONTENT_ID)?.focus()
  }

  return (
    <a
      href={`#${MAIN_CONTENT_ID}`}
      onClick={skip}
      className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-[100] focus:bg-neon-cyan focus:px-4 focus:py-2 focus:text-xs focus:font-black focus:uppercase focus:tracking-widest focus:text-primary-foreground"
    >
      Skip to content
    </a>
  )
}
