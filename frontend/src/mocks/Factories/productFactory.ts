import { faker } from "@faker-js/faker";

const categories = [
  "Outerwear",
  "Tops",
  "Bottoms",
  "Footwear",
  "Accessories",
];

const sizes = ["XS", "S", "M", "L", "XL"];

const colors = [
  "Black",
  "White",
  "Red",
  "Blue",
  "Green",
];

export function createProduct() {
  const hasDiscount = faker.datatype.boolean();

  return {
    id: faker.number.int({ min: 1, max: 999999 }),

    ref: faker.string.alphanumeric(8).toUpperCase(),

    name: faker.commerce.productName(),

    category: faker.helpers.arrayElement(categories),

    price: Number(
      faker.commerce.price({
        min: 20,
        max: 300,
      })
    ),

    image: faker.image.urlPicsumPhotos(),

    description: faker.commerce.productDescription(),

    sizes: faker.helpers.arrayElements(
      sizes,
      faker.number.int({ min: 1, max: 5 })
    ),

    colors: faker.helpers.arrayElements(
      colors,
      faker.number.int({ min: 1, max: 3 })
    ),

    stock: faker.number.int({
      min: 0,
      max: 100,
    }),

    featured: faker.datatype.boolean(),

    isLimited: faker.datatype.boolean(),

    discount: hasDiscount
      ? faker.number.int({ min: 10, max: 80 })
      : undefined,

    createdAt: faker.date.recent({ days: 90 }).toISOString(),
  };
}

export function createProducts(count = 20) {
  return Array.from(
    { length: count },
    createProduct
  );
}
