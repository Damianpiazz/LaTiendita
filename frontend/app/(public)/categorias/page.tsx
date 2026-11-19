import type { Metadata } from "next";

import { CategoriesPage } from "@/features/category/pages/categories-page";

export const metadata: Metadata = {
  title: "Categorías",
};

export default function Page() {
  return <CategoriesPage />;
}
