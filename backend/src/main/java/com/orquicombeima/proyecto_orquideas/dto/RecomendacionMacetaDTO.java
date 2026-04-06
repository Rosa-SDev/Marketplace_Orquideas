package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para mostrarle al frontend cada maceta recomendada para una orquídea
// Combina datos de RecomendacionMaceta y de la Maceta en sí
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RecomendacionMacetaDTO {

    // Por qué se recomienda esta maceta para la orquídea
    private String descripcion;

    // Datos básicos de la maceta para mostrar la tarjeta
    private Long macetaId;
    private String macetaNombre;
    private Double macetaPrecio;
    private String macetaImageUrl;

    // Características propias de la maceta
    private String material;
    private String color;
    private String estilo;
}