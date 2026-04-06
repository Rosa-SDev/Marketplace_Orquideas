package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTO para el detalle completo de una orquídea
// Incluye sus datos básicos, su guía de cuidado y las macetas recomendadas
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrquideaDetalleDTO {

    // Datos básicos de la orquídea (igual que OrquideaDTO)
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

    // Guía de cuidado asociada (puede ser null si todavía no tiene)
    private GuiaCuidadoDTO guiaCuidado;

    // Lista de macetas recomendadas para esta orquídea
    private List<RecomendacionMacetaDTO> recomendaciones;
}