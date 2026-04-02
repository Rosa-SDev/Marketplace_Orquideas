package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaDTO;
import com.orquicombeima.proyecto_orquideas.model.ContenidoPagina;
import com.orquicombeima.proyecto_orquideas.repository.ContenidoPaginaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContenidoPaginaService {

    private final ContenidoPaginaRepository contenidoPaginaRepository;

    // Obtener todo el contenido de la pagina
    public List<ContenidoPaginaDTO> obtenerTodoElContenido() {
        return contenidoPaginaRepository.findAll().stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Obtener contenido de la pagina por tipo
    public List<ContenidoPaginaDTO> obtenerContenidoPorTipo(String tipo) {
        return contenidoPaginaRepository.findByTipoOrderByOrdenAsc(tipo).stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Metodo para convertir a DTO
    private ContenidoPaginaDTO convertirADTO(ContenidoPagina contenidoPagina) {
        return ContenidoPaginaDTO.builder()
                .id(contenidoPagina.getId())
                .tipo(contenidoPagina.getTipo())
                .titulo(contenidoPagina.getTitulo())
                .contenido(contenidoPagina.getContenido())
                .imagenUrl(contenidoPagina.getImagenUrl())
                .orden(contenidoPagina.getOrden())
                .build();
    }
}
