package com.orquicombeima.proyecto_orquideas.dto;

import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPago;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// DTO de salida que se devuelve tanto en el POST /api/pedidos como en el GET /api/pedidos/historial
// En el POST viene con linkPago lleno (para redirigir a Wompi)
// En el GET historial viene con linkPago en null (el pago ya pasó o se canceló)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDTO {

    private Long id;
    private EstadoPedido estado;

    // Estado del pago asociado; puede ser null si todavía no se ha generado el pago
    private EstadoPago estadoPago;

    private List<ItemPedidoDTO> items;
    private DireccionEnvioDTO direccionEnvio;

    private double subtotal;
    private double costoEnvio;
    private double total;

    private LocalDateTime fechaPedido;

    // Link generado por Wompi para que el usuario pague (solo se llena al crear el pedido)
    private String linkPago;

    // Referencia única del pago, sirve para consultarlo después contra Wompi
    private String referenciaPago;
}