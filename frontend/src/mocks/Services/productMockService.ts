import { createProducts } from "@/mocks/Factories/productFactory";

export const getProductsMock = async () => {
  await new Promise((r) => setTimeout(r, 1000));

  return createProducts(20);
};
