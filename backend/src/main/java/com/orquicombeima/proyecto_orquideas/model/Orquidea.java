package com.orquicombeima.proyecto_orquideas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orquideas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "id")


public class Orquidea extends Producto {

    @Column(nullable = false)
    private String variedad;

    @Column(name = "color_flor")
    private String colorFlor;

    @Column(nullable = false)
    private String tamano;

    @Column(name = "nivel_cuidado")
    private String nivelCuidado;

    @Column(name = "tiempo_floracion")
    private String tiempoFloracion;
}