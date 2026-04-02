package com.orquicombeima.proyecto_orquideas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Clase bade para los productos del catálogo
// No se puede usar directamente, solo a través de Orquidea o Maceta

// @Entity: es una tabla en la base de datos
// @Inheritance(JOINED): cada subclase (Orquidea, Maceta) tendrá su propia tabla en la BD, pero comparten el id
@Entity
@Table(name = "productos")
@Inheritance(strategy = InheritanceType.JOINED)
@Data                           // Lombok: genera getters, setters, toString, equals
@NoArgsConstructor              // Lombok: genera constructor vacío
@AllArgsConstructor             // Lombok: genera constructor con todos los campos

public abstract class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)     // el id lo genera MySQL automáticamente
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private Boolean activo = true;               // por defecto todo producto empieza activo
}