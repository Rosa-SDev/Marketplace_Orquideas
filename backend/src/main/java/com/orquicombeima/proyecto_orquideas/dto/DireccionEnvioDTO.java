package com.orquicombeima.proyecto_orquideas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de la dirección de envío
// Lo usamos en dos sitios:
//  - Como entrada dentro de CrearPedidoDTO (cuando el usuario crea el pedido)
//  - Como salida dentro de PedidoDTO (cuando devolvemos el detalle del pedido)
// Por eso lleva validaciones @NotBlank en los campos obligatorios
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionEnvioDTO {

    // En el GET historial devolvemos el id; en el POST entra null y se ignora
    private Long id;

    @NotBlank
    private String nombreDestinatario;

    @NotBlank
    private String telefonoDestinatario;

    @NotBlank
    private String departamento;

    @NotBlank
    private String ciudad;

    @NotBlank
    private String direccion;

    // Opcionales: el usuario puede no llenarlos
    private String codigoPostal;
    private String instruccionesAdicionales;
}