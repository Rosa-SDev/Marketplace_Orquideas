package com.orquicombeima.proyecto_orquideas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Esta clase representa el Carrito de compras de cada usuario
@Entity
@Table(name = "carrito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cada carrito le pertenece a un solo usuario
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Fecha en que se creó el carrito
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Lista de productos dentro del carrito
    // orphanRemoval: si borramos un item de la lista también se borra de la BD
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrito> items = new ArrayList<>();

    // Las reservas que se crean cuando el usuario está por comprar
    // Un carrito tiene varias reservas, una por cada producto en el carrito
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL)
    private List<ReservaCarrito> reservas = new ArrayList<>();
}