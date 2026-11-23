import type { CountryCode } from 'libphonenumber-js'
import { getCountryCallingCode } from 'libphonenumber-js/max'

export const getDialCode = (country: string) => getCountryCallingCode(country as CountryCode)

export const toInternationalPhone = (country: string, digits: string) => `+${getDialCode(country)}${digits}`

export const onlyDigits = (value: string) => value.replace(/\D/g, '')
