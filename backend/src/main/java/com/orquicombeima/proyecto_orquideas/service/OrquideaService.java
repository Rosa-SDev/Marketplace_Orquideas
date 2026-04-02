package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// El controlador le pide datos a este servicio, y este se los pide al repositorio

// @Service le dice a Spring que aquí vive la lógica del negocio
// @RequiredArgsConstructor (Lombok) crea el constructor que inyecta OrquideaRepository automáticamente, sin necesidad de escribir @Autowired
@Service
@RequiredArgsConstructor

public class OrquideaService {

    private final OrquideaRepository orquideaRepository;

    // Devuelve todas las orquideas como una lista de DTOs
    public List<OrquideaDTO> listarTodas() {
        List<Orquidea> orquideas = orquideaRepository.findAll();
        List<OrquideaDTO> resultado = new ArrayList<>();

        for (Orquidea o : orquideas) {
            resultado.add(convertirADTO(o));
        }

        return resultado;
    }

    // Busca una orquídea por su id y la devuelve como DTO
    // Si no existe lanza un error con el id que se buscó
    public OrquideaDTO obtenerPorId(Long id) {
        Orquidea orquidea = orquideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró una orquídea con el id: " + id));
        return convertirADTO(orquidea);
    }

    // Metodo que convierte una entidad Orquidea en un OrquideaDTO
    // La "o" es la orquídea que entra al metodo
    private OrquideaDTO convertirADTO(Orquidea o) {
        OrquideaDTO dto = new OrquideaDTO();
        dto.setId(o.getId());
        dto.setNombre(o.getNombre());
        dto.setPrecio(o.getPrecio());
        dto.setStock(o.getStock());
        dto.setImageUrl(o.getImageUrl());
        dto.setActivo(o.getActivo());
        dto.setVariedad(o.getVariedad());
        dto.setColorFlor(o.getColorFlor());
        dto.setTamanio(o.getTamanio());
        dto.setNivelCuidado(o.getNivelCuidado());
        dto.setTiempoFloracion(o.getTiempoFloracion());
        return dto;
    }
}