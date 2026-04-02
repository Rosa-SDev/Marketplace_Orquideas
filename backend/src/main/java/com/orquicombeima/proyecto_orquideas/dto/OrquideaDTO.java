package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO (Data Transfer Object)
// Es la clase que define exactamente qué datos le enviamos al frontend
// No tiene anotaciones de base de datos porque no es una tabla
// Es solo un "sobre" con la información que necesita ver el usuario

@Data
@NoArgsConstructor
@AllArgsConstructor

public class OrquideaDTO {

    // Campos heredados de Producto
    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private String imageUrl;
    private Boolean activo;

    // Campos propios de Orquidea
    private String variedad;
    private String colorFlor;
    private String tamanio;
    private String nivelCuidado;
    private String tiempoFloracion;
}