package com.orquicombeima.proyecto_orquideas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "recomendacion_maceta")
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionMaceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maceta_id", nullable = false)
    private Maceta maceta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orquidea_id", nullable = false)
    private Orquidea orquidea;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;
}
