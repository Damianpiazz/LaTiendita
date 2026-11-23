import type { AdminOrder } from "@/features/admin/types";

function escapeHtml(value: string): string {
  return value.replace(
    /[&<>"']/g,
    (character) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
      })[character] ?? character,
  );
}

/**
 * Prints one label per loaded order in a hidden iframe, so the admin page stays
 * untouched and the browser's print dialog shows only the labels.
 */
export function printOrderLabels(orders: AdminOrder[]): void {
  if (orders.length === 0) return;

  const iframe = document.createElement("iframe");
  iframe.setAttribute("aria-hidden", "true");
  iframe.title = "Etiquetas de pedidos";
  iframe.style.position = "fixed";
  iframe.style.right = "0";
  iframe.style.bottom = "0";
  iframe.style.width = "0";
  iframe.style.height = "0";
  iframe.style.border = "0";
  document.body.appendChild(iframe);

  const frameWindow = iframe.contentWindow;
  const doc = frameWindow?.document;
  if (!frameWindow || !doc) {
    iframe.remove();
    return;
  }

  const labels = orders
    .map(
      (order) => `<section class="label">
        <strong>${escapeHtml(order.id)}</strong>
        <span>${escapeHtml(order.customer)}</span>
        <span>${escapeHtml(order.email)}</span>
        <span>${escapeHtml(order.placedAt)}</span>
        <span>${order.itemCount} artículo${order.itemCount === 1 ? "" : "s"}</span>
      </section>`,
    )
    .join("");

  doc.open();
  doc.write(`<!doctype html><html lang="es"><head><meta charset="utf-8" />
    <title>Etiquetas de pedidos</title><style>
      @page { size: 100mm 50mm; margin: 5mm; }
      body { font-family: Arial, sans-serif; margin: 0; color: #111; }
      .label { display: flex; flex-direction: column; gap: 4px;
        height: 40mm; padding: 4mm; break-after: page; page-break-after: always; }
      .label strong { font-size: 20px; }
      .label span { font-size: 12px; }
    </style></head><body>${labels}</body></html>`);
  doc.close();

  frameWindow.addEventListener("afterprint", () => iframe.remove(), { once: true });
  frameWindow.focus();
  frameWindow.print();
}
