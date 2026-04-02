package com.orquicombeima.proyecto_orquideas.model;

import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "pedido")
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> items = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "direccion_envio_id", nullable = false)
    private DireccionEnvio direccionEnvio;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private PagoWompi pago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(nullable = false)
    private double subtotal;

    @Column(nullable = false)
    private double costoEnvio;

    @Column(nullable = false)
    private double total;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPedido;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void creacionPedido() {
        this.fechaPedido = LocalDateTime.now();
        this.estado = EstadoPedido.PENDIENTE;
    }

    @PreUpdate
    protected void actualizacionPedido() {
        this.fechaActualizacion = LocalDateTime.now();
    }

}
