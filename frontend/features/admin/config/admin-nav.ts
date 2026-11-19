import type { AdminNavigationCounts } from "@/features/admin/types";

/** One sidebar entry; `badge` picks its count from `GET /admin/navigation`. */
export type AdminNavItem = {
  label: string;
  href: string;
  badge?: keyof AdminNavigationCounts;
};

export type AdminNavSection = {
  title: string;
  items: AdminNavItem[];
};

/**
 * Sidebar map of the admin shell, in the order the frame draws it.
 *
 * Only `/admin` exists today; the other entries are the planned screens, the
 * same criterion the public shell uses for its unfinished links.
 */
export const adminNavSections: AdminNavSection[] = [
  {
    title: "General",
    items: [
      { label: "Resumen", href: "/admin" },
      { label: "Pedidos", href: "/admin/pedidos", badge: "pendingOrders" },
      { label: "Productos", href: "/admin/productos" },
      { label: "Categorías", href: "/admin/categorias" },
      { label: "Pagos", href: "/admin/pagos", badge: "payments" },
      { label: "Clientes", href: "/admin/clientes" },
    ],
  },
  {
    title: "Sistema",
    items: [
      { label: "Reportes", href: "/admin/reportes" },
      { label: "Configuración", href: "/admin/configuracion" },
    ],
  },
];
