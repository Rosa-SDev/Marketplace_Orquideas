package com.orquicombeima.proyecto_orquideas.model;

import com.orquicombeima.proyecto_orquideas.model.enums.EstadoReserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Cuando el usuario va a comprar, se crea una reserva para apartar el stock
// Un carrito puede tener varias reservas, una por cada producto distinto
// Cada reserva puede estar activa, expirada, confirmada, liberada o cancelada
@Entity
@Table(name = "reservas_carrito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A qué carrito pertenece esta reserva
    // @ManyToOne porque un carrito puede tener varias reservas (una por producto)
    @ManyToOne
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    // Qué producto está siendo reservado
    @NotNull
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Cuántas unidades se están reservando
    // @Positive para que nunca se guarde un 0 o negativo y rompa el stock
    @Positive
    @Column(name = "cantidad_reservada", nullable = false)
    private Integer cantidadReservada;

    // Estado actual de la reserva
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    // LocalDateTime guarda fecha y hora juntas
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;
}