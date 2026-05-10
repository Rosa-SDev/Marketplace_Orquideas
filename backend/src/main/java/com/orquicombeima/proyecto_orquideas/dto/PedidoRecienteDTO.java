package com.orquicombeima.proyecto_orquideas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedidoRecienteDTO {

    private long id;
    private String nombreCliente;
    private double total;
    private String estado;
    private String tiempoTranscurrido;
}
