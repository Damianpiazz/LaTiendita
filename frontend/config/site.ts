export type NavItem = {
  label: string;
  href: string;
};

export type BenefitIcon = "shipping" | "payment" | "returns" | "support";

export type Benefit = {
  icon: BenefitIcon;
  title: string;
  note: string;
};

export type FooterColumn = {
  title: string;
  links: NavItem[];
};

/**
 * Public contact endpoints. Replace these values with the store's real
 * accounts before launch; they are kept in one place so the header, footer
 * and newsletter never drift apart.
 */
const contact = {
  email: "hola@latiendita.com",
  whatsapp: "https://wa.me/5491122334455",
  instagram: "https://www.instagram.com/latiendita/",
} as const;

/**
 * Static copy and navigation shared by the public shell.
 *
 * Anything that comes from the catalog API (categories, products) is not here.
 */
export const siteConfig = {
  name: "La Tiendita",
  description:
    "Tecnología, hogar, ropa, deportes y juguetes. Envíos a todo el país en 24-48 hs.",
  nav: [
    { label: "Inicio", href: "/" },
    { label: "Categorías", href: "/categorias" },
    { label: "Ofertas", href: "#productos" },
    { label: "Contacto", href: "#contacto" },
  ] satisfies NavItem[],
  searchPlaceholder: "Buscar productos...",
  contact,
  benefits: [
    {
      icon: "shipping",
      title: "Envíos a todo el país",
      note: "Recibilo en 24-48 hs",
    },
    { icon: "payment", title: "Pagos seguros", note: "Tarjeta, transferencia y más" },
    { icon: "returns", title: "Devoluciones sin cargo", note: "30 días de garantía" },
    { icon: "support", title: "Soporte humano", note: "Atención real, sin robots" },
  ] satisfies Benefit[],
  productPerks: [
    {
      title: "Envío gratis en compras mayores a $ 20.000",
      note: "Recibilo en 24-48 hs en todo el país",
    },
    {
      title: "30 días de garantía",
      note: "Devoluciones sin cargo, sin preguntas",
    },
    {
      title: "Pago seguro",
      note: "Tarjeta, transferencia, débito y efectivo",
    },
  ],
  newsletter: {
    title: "Suscribite a las novedades",
    copy: "Ofertas, restocks y features nuevos de La Tiendita.",
    placeholder: "tu@email.com",
    action: "Suscribirme",
  },
  footer: {
    tagline:
      "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.",
    socials: [
      { label: "Instagram", href: contact.instagram },
      { label: "WhatsApp", href: contact.whatsapp },
      { label: "Email", href: `mailto:${contact.email}` },
    ] satisfies NavItem[],
    columns: [
      {
        title: "Tienda",
        links: [
          { label: "Productos", href: "/productos" },
          { label: "Categorías", href: "/categorias" },
          { label: "Ofertas", href: "#productos" },
        ],
      },
      {
        title: "Ayuda",
        links: [
          { label: "Envíos", href: "/envios" },
          { label: "Devoluciones", href: "/devoluciones" },
          { label: "Preguntas frecuentes", href: "/faq" },
        ],
      },
      {
        title: "Contacto",
        links: [
          { label: "WhatsApp", href: contact.whatsapp },
          { label: "Email", href: `mailto:${contact.email}` },
          { label: "Instagram", href: contact.instagram },
        ],
      },
    ] satisfies FooterColumn[],
    legal: "© 2026 La Tiendita",
  },
} as const;
