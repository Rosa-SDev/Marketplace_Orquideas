package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.model.ItemPedido;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ContactoWhatsAppService {

    @Value("${contacto.whatsapp.numero}")
    private String numero;

    private final PedidoRepository pedidoRepository;

    // Función para generar el enlace de contacto
    public String generarEnlaceContacto(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("No se encontró el pedido: " + idPedido));

        String mensaje = construirMensaje(pedido);
        String mensajeCodificado = URLEncoder.encode(mensaje, StandardCharsets.UTF_8);

        return "https://wa.me/" + numero + "?text=" + mensajeCodificado;
    }

    private String construirMensaje(Pedido pedido) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Hola, acabo de realizar el pedido #").append(pedido.getId()).append("\n\n");
        mensaje.append("Productos:\n");

        for (ItemPedido item : pedido.getItems()) {
            mensaje.append("- ").append(item.getProducto().getNombre())
                    .append(" x").append(item.getCantidad())
                    .append(" -> $").append(String.format("%,.0f", item.getPrecioUnitario()))
                    .append("\n");
        }

        mensaje.append("\nSubtotal: $").append(String.format("%,.0f", pedido.getSubtotal()));
        mensaje.append("\nEnvío: $").append(String.format("%,.0f", pedido.getCostoEnvio()));
        mensaje.append("\nTotal: $").append(String.format("%,.0f", pedido.getTotal()));
        mensaje.append("\n\nDireccion de envio: ")
                .append(pedido.getDireccionEnvio().getDireccion()).append(", ")
                .append(pedido.getDireccionEnvio().getCiudad()).append(", ")
                .append(pedido.getDireccionEnvio().getDepartamento()).append(", ");

        return mensaje.toString();
    }
}
