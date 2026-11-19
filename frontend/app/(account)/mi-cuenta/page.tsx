import type { Metadata } from "next";

import { ProfilePage } from "@/features/customer";

export const metadata: Metadata = {
  title: "Mi cuenta",
};

export default function Page() {
  return <ProfilePage />;
}
