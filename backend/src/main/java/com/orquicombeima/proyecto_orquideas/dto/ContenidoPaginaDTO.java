package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContenidoPaginaDTO {
    private Long id;
    private String tipo;
    private String titulo;
    private String contenido;
    private String imagenUrl;
    private Integer orden;

}
