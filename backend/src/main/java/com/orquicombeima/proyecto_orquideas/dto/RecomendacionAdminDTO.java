package com.orquicombeima.proyecto_orquideas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecomendacionAdminDTO {

    @NotNull
    private Long idOrquidea;

    @NotNull
    private Long idMaceta;

    @NotBlank
    private String descripcion;
}
