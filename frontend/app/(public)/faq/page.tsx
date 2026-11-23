import type { Metadata } from "next";

import { InfoPage } from "@/features/content/components/info-page";

export const metadata: Metadata = {
  title: "Preguntas frecuentes",
};

export default function FaqPage() {
  return (
    <InfoPage
      title="Preguntas frecuentes"
      intro="Respuestas rápidas a las dudas más comunes sobre tu compra."
      sections={[
        {
          title: "¿Cómo creo una cuenta?",
          body: "Tocá «Crear cuenta» en el encabezado, completá tus datos y validá tu email. Desde entonces vas a poder seguir tus pedidos y guardar tus direcciones.",
        },
        {
          title: "¿Puedo cambiar la dirección de un pedido?",
          body: "Solo mientras esté pendiente de despacho. Escribinos con el número de pedido y la dirección nueva; una vez despachado no es posible redirigirlo.",
        },
        {
          title: "¿Qué medios de pago aceptan?",
          body: "Tarjetas de crédito y débito, transferencia y efectivo. El detalle disponible se muestra en el paso de pago del checkout.",
        },
        {
          title: "¿Puedo dejar una reseña?",
          body: "Sí. Podés reseñar los productos de pedidos acreditados, enviados o entregados. Las reseñas se publican después de la moderación.",
        },
      ]}
    />
  );
}
