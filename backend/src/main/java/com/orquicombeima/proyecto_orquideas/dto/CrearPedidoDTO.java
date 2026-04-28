package com.orquicombeima.proyecto_orquideas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de entrada para POST /api/pedidos
// El usuario manda solo la dirección de envío en el body porque:
//  - El carrito se lee del usuario autenticado (no se manda en el body)
//  - Los precios y subtotales se calculan en el backend, no se confía en lo que mande el cliente
//  - El total se recalcula en el servicio sumando los items + costo de envío
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPedidoDTO {

    // @Valid hace que se ejecuten también las validaciones de DireccionEnvioDTO
    // Si solo pusiéramos @NotNull, validaría que existe el objeto pero no sus campos
    @NotNull
    @Valid
    private DireccionEnvioDTO direccionEnvio;
}