export const DAY = 86_400_000
export const HOUR = 3_600_000

export interface Random {
  next: () => number
  int: (min: number, max: number) => number
  pick: <T>(items: readonly T[]) => T
  chance: (probability: number) => boolean
}

export const createRandom = (seed: number): Random => {
  let state = seed

  const next = () => {
    state = (state + 0x6d2b79f5) | 0
    let t = Math.imul(state ^ (state >>> 15), 1 | state)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t

    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }

  return {
    next,
    int: (min, max) => Math.floor(next() * (max - min + 1)) + min,
    pick: (items) => items[Math.floor(next() * items.length)],
    chance: (probability) => next() < probability
  }
}

export const roundMoney = (value: number) => Math.round(value * 100) / 100

export const toIso = (timestamp: number) => new Date(timestamp).toISOString()
