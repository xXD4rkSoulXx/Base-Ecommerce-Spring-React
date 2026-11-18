const LOCALE = 'pt-PT'

const priceFormatter = new Intl.NumberFormat(LOCALE, { style: 'currency', currency: 'EUR' })
const dateFormatter = new Intl.DateTimeFormat(LOCALE, { dateStyle: 'medium' })
const dateTimeFormatter = new Intl.DateTimeFormat(LOCALE, { dateStyle: 'medium', timeStyle: 'short' })
const compactDateTimeFormatter = new Intl.DateTimeFormat(LOCALE, { dateStyle: 'short', timeStyle: 'short' })
const percentChangeFormatter = new Intl.NumberFormat(LOCALE, { maximumFractionDigits: 1, signDisplay: 'exceptZero' })
const numberFormatter = new Intl.NumberFormat(LOCALE)
const ratingFormatter = new Intl.NumberFormat(LOCALE, { maximumFractionDigits: 1 })
const countryNames = new Intl.DisplayNames('en', { type: 'region' })

export const formatPrice = (value: number) => priceFormatter.format(value)

export const formatDate = (value: string) => dateFormatter.format(new Date(value))

export const formatDateTime = (value: string) => dateTimeFormatter.format(new Date(value))

export const formatCompactDateTime = (value: string) => compactDateTimeFormatter.format(new Date(value))

export const formatPercentChange = (value: number) => percentChangeFormatter.format(value)

export const formatNumber = (value: number) => numberFormatter.format(value)

export const formatRating = (value: number) => `${ratingFormatter.format(value)} / 5`

export const formatCountry = (code: string) => countryNames.of(code) ?? code

export const formatSize = (size: string) => {
  if (size === 'ONE_SIZE') {
    return 'One size'
  }

  return size.startsWith('_') ? size.substring(1) : size
}

export const formatCategory = (category: string) => category.charAt(0) + category.slice(1).toLowerCase()

export const pluralize = (count: number, word: string) => `${count} ${word}${count === 1 ? '' : 's'}`
