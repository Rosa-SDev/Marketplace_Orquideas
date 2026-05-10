package com.orquicombeima.proyecto_orquideas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// DTO que el panel de administración manda al backend cuando crea o actualiza una guía de cuidado
// Va por multipart/form-data porque incluye la imagen
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GuiaAdminDTO {

    @NotNull
    private Long idOrquidea;

    @NotBlank
    private String titulo;

    @NotBlank
    private String variedad;

    @NotBlank
    private String contenido;

    @NotBlank
    private String frecuenciaRiego;

    @NotBlank
    private String luzRequerida;

    @NotBlank
    private String temperaturaIdeal;

    @NotBlank
    private String fertilizacion;

    // En POST puede venir, en PUT puede venir null si no se cambia la imagen
    private MultipartFile imagen;
}