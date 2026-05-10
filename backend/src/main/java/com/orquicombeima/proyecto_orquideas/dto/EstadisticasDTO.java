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
public class EstadisticasDTO {

    private double ventasMes;
    private int pedidosMes;
    private int clientesRegistrados;
    private int pedidosPendientes;
    private List<VentasMesDTO> ventasPorMes;
}
