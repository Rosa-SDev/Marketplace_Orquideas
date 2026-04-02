package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.service.OrquideaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController indica que esta clase recibe peticiones HTTP y devuelve JSON
// @RequestMapping define la ruta base: todos los endpoints empiezan con /api/orquideas
// @RequiredArgsConstructor inyecta OrquideaService automáticamente
@RestController
@RequestMapping("/api/orquideas")
@RequiredArgsConstructor

public class OrquideaController {

    private final OrquideaService orquideaService;

    // GET /api/orquideas: devuelve todas las orquídeas
    // ResponseEntity<List<OrquideaDTO>> es el "sobre" que envuelve la respuesta HTTP con su código de estado y el contenido
    @GetMapping
    public ResponseEntity<List<OrquideaDTO>> listarTodas() {
        List<OrquideaDTO> orquideas = orquideaService.listarTodas();
        return ResponseEntity.ok(orquideas);
    }

    // GET /api/orquideas/{id} : devuelve una sola orquídea por su id
    // @PathVariable extrae el {id} de la URL y lo pasa al metodo como parametro
    @GetMapping("/{id}")
    public ResponseEntity<OrquideaDTO> obtenerPorId(@PathVariable Long id) {
        OrquideaDTO orquidea = orquideaService.obtenerPorId(id);
        return ResponseEntity.ok(orquidea);
    }
}