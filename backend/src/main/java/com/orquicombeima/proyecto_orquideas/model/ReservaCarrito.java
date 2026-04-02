package com.orquicombeima.proyecto_orquideas.model;

import com.orquicombeima.proyecto_orquideas.model.enums.EstadoReserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Cuando el usuario va a comprar, se crea una reserva para apartar el stock
// Esta reserva puede estar activa, expirada, confirmada, liberada o cancelada
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
    @OneToOne
    @JoinColumn(name = "carrito_id", nullable = false, unique = true)
    private Carrito carrito;

    // Estado actual de la reserva
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    // LocalDateTime: Guarda fecha y hora juntas, es más preciso que Date
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;
}