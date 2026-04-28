package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de salida para cada item dentro de un pedido
// Aplanamos los datos del producto (nombre, imagen) para que el frontend no tenga
// que hacer otra llamada al detalle del producto solo para mostrarlos
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedidoDTO {

    private Long id;
    private Long idProducto;
    private String nombreProducto;
    private String imagenUrl;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
}