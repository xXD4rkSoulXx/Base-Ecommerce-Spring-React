import type { ComponentType } from 'react'

export const lazyRoute =
  <K extends string>(load: () => Promise<Record<K, ComponentType>>, name: K) =>
  async () => ({ Component: (await load())[name] })
