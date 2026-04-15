package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarritoDTO {

    private Long id;
    private Long idProducto;
    private String nombreProducto;
    private String imagenUrl;
    private double precioUnitario;
    private int cantidad;
    private double subtotal;

}
