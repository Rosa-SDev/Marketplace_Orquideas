package com.orquicombeima.proyecto_orquideas.model;

import com.orquicombeima.proyecto_orquideas.model.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Esta clase representa a los usuarios del sistema que pueden ser clientes o administradores según su rol
@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank                           // valida que el campo no sea ni null ni vacío
    @Column(nullable = false)
    private String nombre;

    @Email                              // valida que el texto tenga formato de correo electrónico
    @NotBlank
    @Column(nullable = false, unique = true)        // unique: no pueden existir dos usuarios con el mismo correo
    private String email;

    @NotNull                            // valida que el campo no sea null
    @Enumerated(EnumType.STRING)        // guarda el texto "CLIENTE" o "ADMINISTRADOR" en la BD
    @Column(nullable = false)
    private Rol rol;

    // Cada usuario tiene exactamente un carrito
    // mappedBy = "usuario" significa que la relación la maneja el campo "usuario" en Carrito
    // cascade = ALL: si se elimina el usuario, se elimina su carrito también
    // fetch = LAZY: el carrito NO se carga automáticamente, solo cuando se pide explícitamente
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Carrito carrito;
}