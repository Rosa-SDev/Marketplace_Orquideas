package com.orquicombeima.proyecto_orquideas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "guia_cuidado")
@AllArgsConstructor
@NoArgsConstructor
public class GuiaCuidado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Revisar esta relacion
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orquidea_id", nullable = false)
    private Orquidea orquidea;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String variedad;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(nullable = false)
    private String frecuenciaRiego;

    @Column(nullable = false)
    private String luzRequerida;

    @Column(nullable = false)
    private String temperaturaIdeal;

    @Column(nullable = false)
    private String fertilizacion;

    private String imageUrl;
}
