package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaCuidadoDTO {
    private Long id;
    private String titulo;
    private String variedad;
    private String contenido;
    private String frecuenciaRiego;
    private String luzRequerida;
    private String temperaturaIdeal;
    private String fertilizacion;
    private String imageUrl;
    private Long idOrquidea;
    private String nombreOrquidea;
}
