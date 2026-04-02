package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MacetaDTO {
    // Campos heredados de Producto
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String imageUrl;
    private Boolean activo;

    // Campos propios de Maceta
    private String material;
    private double diametroCm;
    private String color;
    private String estilo;
}
