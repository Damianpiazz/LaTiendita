// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

const pathname = vi.hoisted(() => ({ value: "/admin" }));
const push = vi.hoisted(() => vi.fn());

vi.mock("next/navigation", () => ({
  usePathname: () => pathname.value,
  useRouter: () => ({ push }),
}));

import { AdminShell } from "./admin-shell";

const counts = { pendingOrders: 2, payments: 3, alerts: 4 };

const user = {
  id: "elena-1",
  email: "elena.admin@latiendita.test",
  givenName: "Elena",
  familyName: "Administradora",
  roles: ["cliente", "admin"],
};

describe("AdminShell identity", () => {
  afterEach(() => cleanup());

  it("shows the signed-in admin in the sidebar and topbar", () => {
    render(
      <AdminShell counts={counts} user={user}>
        <div>Contenido</div>
      </AdminShell>,
    );

    expect(screen.getByText("Elena Administradora")).toBeInTheDocument();
    expect(screen.getByText("Administrador")).toBeInTheDocument();
    expect(screen.getAllByText("EA")).toHaveLength(2);
    expect(
      screen.getByTitle("elena.admin@latiendita.test · Administrador"),
    ).toBeInTheDocument();
    expect(screen.queryByText("Damián Pérez")).not.toBeInTheDocument();
    expect(screen.queryByText("DP")).not.toBeInTheDocument();
  });

  it("falls back to a guest identity when there is no session", () => {
    render(
      <AdminShell counts={counts} user={null}>
        <div>Contenido</div>
      </AdminShell>,
    );

    expect(screen.getAllByText("--")).toHaveLength(2);
    expect(screen.getAllByText("Sin sesión").length).toBeGreaterThan(0);
    expect(screen.getByText("Invitado")).toBeInTheDocument();
  });
});
