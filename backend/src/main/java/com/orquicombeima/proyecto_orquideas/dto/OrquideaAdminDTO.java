package com.orquicombeima.proyecto_orquideas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// DTO que el panel de administración manda al backend cuando crea o actualiza una orquídea
// Va por multipart/form-data porque incluye la imagen
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrquideaAdminDTO {

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    @Positive
    private Double precio;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    @NotBlank
    private String variedad;

    private String colorFlor;

    @NotBlank
    private String tamanio;

    private String nivelCuidado;

    private String tiempoFloracion;

    private Boolean activo;

    // En POST puede venir, en PUT puede venir null si no se cambia la imagen
    private MultipartFile imagen;
}