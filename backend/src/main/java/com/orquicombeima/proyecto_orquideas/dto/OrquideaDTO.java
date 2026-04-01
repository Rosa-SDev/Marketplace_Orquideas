package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor


public class OrquideaDTO {

    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private String imageUrl;
    private Boolean activo;
    private String variedad;
    private String colorFlor;
    private String tamanio;
    private String nivelCuidado;
    private String tiempoFloracion;
}