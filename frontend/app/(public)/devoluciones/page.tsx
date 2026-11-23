import type { Metadata } from "next";

import { InfoPage } from "@/features/content/components/info-page";

export const metadata: Metadata = {
  title: "Devoluciones",
};

export default function ReturnsInfoPage() {
  return (
    <InfoPage
      title="Devoluciones"
      intro="Si algo no salió como esperabas, tenés 30 días para pedir la devolución."
      sections={[
        {
          title: "Cómo pedirla",
          body: "Escribinos desde el formulario de contacto con el número de pedido y el motivo. Te respondemos dentro de las 48 horas hábiles con la etiqueta de devolución sin cargo.",
        },
        {
          title: "Condiciones",
          body: "El producto debe estar sin uso y con su embalaje original.Los artículos de higiene y los productos personalizados no admiten devolución, salvo falla de fabricación.",
        },
        {
          title: "Reembolsos",
          body: "Una vez que recibimos y revisamos el paquete, devolvemos el importe al mismo medio de pago. El crédito se acredita entre 5 y 10 días hábiles según la entidad.",
        },
      ]}
    />
  );
}
