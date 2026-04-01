package com.orquicombeima.proyecto_orquideas.model;

import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPago;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "pago_wompi")
@AllArgsConstructor
@NoArgsConstructor
public class PagoWompi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(unique = true)
    private String transaccionId;

    @Column(nullable = false)
    private String referenciaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado;

    @Column(nullable = false)
    private double monto;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private String metodoPago;
    private LocalDateTime fechaPago;

    @PrePersist
    public void antesCreacionPago() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoPago.PENDIENTE;
    }

}
