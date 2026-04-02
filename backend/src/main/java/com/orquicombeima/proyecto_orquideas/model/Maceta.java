package com.orquicombeima.proyecto_orquideas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Maceta hereda de Producto igual que Orquidea
// Tiene su propia tabla "macetas" con sus campos específicos, conectada a "productos" por el mismo id
@Entity
@Table(name = "macetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "id")

public class Maceta extends Producto {

    @Column(nullable = false)
    private String material;

    @Column(nullable = false)
    private double diametroCm;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String estilo;
}