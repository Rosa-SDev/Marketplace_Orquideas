package com.orquicombeima.proyecto_orquideas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "direccion_envio")
@AllArgsConstructor
@NoArgsConstructor
public class DireccionEnvio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "direccionEnvio")
    private Pedido pedido;

    @Column(nullable = false)
    private String nombreDestinatario;

    @Column(nullable = false)
    private String telefonoDestinatario;

    @Column(nullable = false)
    private String departamento;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false)
    private String direccion;

    private String codigoPostal;
    private String instruccionesAdicionales;
}
