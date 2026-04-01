package com.orquicombeima.proyecto_orquideas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String color;

    @Column(nullable = false)
    private String estilo;
}