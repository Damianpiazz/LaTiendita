import type { Metadata } from "next";

import { InfoPage } from "@/features/content/components/info-page";

export const metadata: Metadata = {
  title: "Envíos",
};

export default function ShippingInfoPage() {
  return (
    <InfoPage
      title="Envíos"
      intro="Trabajamos con envíos a todo el país y te avisamos cada paso del camino."
      sections={[
        {
          title: "Plazos",
          body: "Los pedidos acreditados se despachan dentro de las 24 horas hábiles. La entrega demora entre 24 y 48 horas en las grandes ciudades y hasta 5 días hábiles en el resto del país.",
        },
        {
          title: "Costos",
          body: "El envío es gratuito en compras superiores a $ 20.000. Para el resto de los pedidos, el costo se calcula al ingresar la dirección en el checkout, antes de confirmar la compra.",
        },
        {
          title: "Seguimiento",
          body: "Cuando el pedido se despache vas a recibir el número de seguimiento por email. También podés consultar el estado desde Mis pedidos, dentro de tu cuenta.",
        },
      ]}
    />
  );
}
