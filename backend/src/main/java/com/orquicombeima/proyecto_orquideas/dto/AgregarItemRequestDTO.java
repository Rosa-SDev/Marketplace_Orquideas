package com.orquicombeima.proyecto_orquideas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Este DTO es lo que el frontend manda en el body del POST /api/carrito/agregar
// Contiene el id del producto que se quiere agregar y la cantidad
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarItemRequestDTO {

    // El producto que se quiere agregar al carrito
    @NotNull
    private Long idProducto;

    // Cuántas unidades quiere agregar
    // @Min(1) para que siempre sea mínimo 1 (no aceptamos 0 ni negativos)
    @NotNull
    @Min(1)
    private Integer cantidad;
}