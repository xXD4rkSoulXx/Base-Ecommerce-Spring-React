export const getDiscountedPrice = (price: number, discountPercentage: number) =>
  Math.round((Math.round(price * 100) * (100 - discountPercentage)) / 100) / 100
