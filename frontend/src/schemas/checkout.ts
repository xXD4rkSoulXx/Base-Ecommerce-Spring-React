import { isValidPhoneNumber } from 'libphonenumber-js/max'
import { postcodeValidator, postcodeValidatorExistsForCountry } from 'postcode-validator'
import { z } from 'zod'
import { nameSchema } from '@/schemas/common'
import { toInternationalPhone } from '@/utils/phone'

const requiredText = (label: string, max: number) =>
  z.string().trim().min(1, `${label} is required`).max(max, `${label} must be at most ${max} characters`)

const optionalText = (label: string, max: number) =>
  z.string().trim().max(max, `${label} must be at most ${max} characters`)

export const checkoutAddressSchema = z
  .object({
    recipientName: nameSchema('Recipient name'),
    addressLine1: requiredText('Address line 1', 100),
    addressLine2: optionalText('Address line 2', 100),
    city: requiredText('City', 100),
    state: optionalText('State', 100),
    postalCode: requiredText('Postal code', 20),
    country: z.string().regex(/^[A-Z]{2}$/, 'Country must be a 2-letter code, e.g. PT'),
    phoneCountry: z.string().regex(/^[A-Z]{2}$/, 'Dial code is required'),
    phone: z.string().min(1, 'Phone is required').max(15, 'Phone must be at most 15 digits')
  })
  .superRefine((address, context) => {
    if (address.phone && !isValidPhoneNumber(toInternationalPhone(address.phoneCountry, address.phone))) {
      context.addIssue({ code: 'custom', path: ['phone'], message: 'Enter a valid phone number' })
    }

    if (
      address.postalCode &&
      postcodeValidatorExistsForCountry(address.country) &&
      !postcodeValidator(address.postalCode, address.country)
    ) {
      context.addIssue({ code: 'custom', path: ['postalCode'], message: 'Enter a valid postal code for this country' })
    }
  })

export const promoCodeSchema = z.object({
  code: z.string().trim().min(1, 'Enter a promo code').max(20, 'Invalid discount code')
})

export type CheckoutAddressForm = z.infer<typeof checkoutAddressSchema>
export type PromoCodeForm = z.infer<typeof promoCodeSchema>
