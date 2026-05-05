package com.orquicombeima.proyecto_orquideas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContenidoPaginaAdminDTO {

    @NotBlank
    private String tipo;

    @NotBlank
    private String titulo;

    @NotBlank
    private String contenido;

    @NotNull
    private int orden;

    private MultipartFile imagen;
}
