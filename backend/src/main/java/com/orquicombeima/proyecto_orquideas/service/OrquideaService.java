package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// @Service le dice a Spring que aquí vive la lógica de negocio
// @RequiredArgsConstructor (Lombok) crea el constructor que inyecta OrquideaRepository automáticamente, sin necesidad de escribir @Autowired
@Service
@RequiredArgsConstructor
public class OrquideaService {

    private final OrquideaRepository orquideaRepository;

    // Devuelve la lista de todas las orquídeas como DTOs
    public List<OrquideaDTO> listarTodas() {
        return orquideaRepository.findAll()         // trae todas las orquídeas de la BD
                .stream()                           // las convierte en flujo para procesarlas una por una
                .map(this::convertirADTO)           // a cada orquídea le aplica el metodo convertirADTO
                .collect(Collectors.toList());      // junta los resultados en una lista
    }

    // Busca una orquídea por su id y la devuelve como DTO
    // Si no existe, lanza un error con mensaje descriptivo
    public OrquideaDTO obtenerPorId(Long id) {
        Orquidea orquidea = orquideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orquídea no encontrada con id: " + id));
        return convertirADTO(orquidea);
    }

    // Metodo privado que convierte una entidad Orquidea en un OrquideaDTO
    private OrquideaDTO convertirADTO(Orquidea o) {
        return new OrquideaDTO(
                o.getId(),
                o.getNombre(),
                o.getPrecio(),
                o.getStock(),
                o.getImageUrl(),
                o.getActivo(),
                o.getVariedad(),
                o.getColorFlor(),
                o.getTamanio(),
                o.getNivelCuidado(),
                o.getTiempoFloracion()
        );
    }
}