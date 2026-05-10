package com.orquicombeima.proyecto_orquideas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MacetaAdminDTO {

    @NotBlank
    private String nombre;

    @NotBlank
    private String descripcion;

    @NotNull
    @Positive
    private double precio;

    @NotNull
    @Positive
    private int stock;

    @NotBlank
    private String material;

    @NotNull
    @Positive
    private double diametroCm;

    @NotBlank
    private String color;

    @NotBlank
    private String estilo;

    private Boolean activo;

    private MultipartFile imagen;
}
