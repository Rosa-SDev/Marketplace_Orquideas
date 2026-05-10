package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecomendacionDTO {

    private Long id;
    private Long idOrquidea;
    private String nombreOrquidea;
    private Long idMaceta;
    private String nombreMaceta;
    private String descripcion;

}
