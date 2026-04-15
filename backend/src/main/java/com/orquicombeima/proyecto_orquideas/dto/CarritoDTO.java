package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarritoDTO {

    private Long id;
    private Long idUsuario;
    private List<ItemCarritoDTO> items;
    private double total;

}
