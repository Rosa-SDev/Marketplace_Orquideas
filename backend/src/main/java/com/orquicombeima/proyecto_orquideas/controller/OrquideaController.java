package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.dto.OrquideaDetalleDTO;
import com.orquicombeima.proyecto_orquideas.dto.RecomendacionMacetaDTO;
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

    // GET /api/orquideas: devuelve orquídeas con filtros opcionales y paginación
    // Ejemplos:
    //   /api/orquideas
    //   /api/orquideas?variedad=Cattleya
    //   /api/orquideas?colorFlor=Rojo&precioMax=50000
    //   /api/orquideas?page=0&size=9
    // @RequestParam(required = false) significa que el parámetro es opcional — si no llega, vale null
    @GetMapping
    public ResponseEntity<List<OrquideaDTO>> listarTodas(
            @RequestParam(required = false) String variedad,
            @RequestParam(required = false) String colorFlor,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        List<OrquideaDTO> orquideas = orquideaService.listarTodas(variedad, colorFlor, precioMin, precioMax, page, size);
        return ResponseEntity.ok(orquideas);
    }

    // GET /api/orquideas/{id}: devuelve el detalle completo de una orquídea
    // Incluye su guía de cuidado y las macetas recomendadas
    @GetMapping("/{id}")
    public ResponseEntity<OrquideaDetalleDTO> obtenerDetalle(@PathVariable Long id) {
        OrquideaDetalleDTO detalle = orquideaService.obtenerDetallePorId(id);
        return ResponseEntity.ok(detalle);
    }

    // GET /api/orquideas/{id}/recomendaciones: devuelve solo las macetas recomendadas para una orquídea
    @GetMapping("/{id}/recomendaciones")
    public ResponseEntity<List<RecomendacionMacetaDTO>> obtenerRecomendaciones(@PathVariable Long id) {
        List<RecomendacionMacetaDTO> recomendaciones = orquideaService.obtenerRecomendaciones(id);
        return ResponseEntity.ok(recomendaciones);
    }
}