package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaDTO;
import com.orquicombeima.proyecto_orquideas.service.ContenidoPaginaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contenido")
@RequiredArgsConstructor
public class ContenidoPaginaController {

    private final ContenidoPaginaService contenidoPaginaService;

    // Endpoint para obtener todo el contenido de la pagina
    @GetMapping
    public ResponseEntity<List<ContenidoPaginaDTO>> obtenerTodoElContenido() {
         return ResponseEntity.ok(contenidoPaginaService.obtenerTodoElContenido());
    }

    // Endpoint para obtener contenido de la pagina por tipo
    @GetMapping("/{tipo}")
    public ResponseEntity<List<ContenidoPaginaDTO>> obtenerContenidoPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(contenidoPaginaService.obtenerContenidoPorTipo(tipo));
    }
}
