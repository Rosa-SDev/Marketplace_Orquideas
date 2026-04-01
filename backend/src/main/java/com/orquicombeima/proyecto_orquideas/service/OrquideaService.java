package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor


public class OrquideaService {

    private final OrquideaRepository orquideaRepository;

    public List<OrquideaDTO> listarTodas() {
        return orquideaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public OrquideaDTO obtenerPorId(Long id) {
        Orquidea orquidea = orquideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orquídea no encontrada con id: " + id));
        return convertirADTO(orquidea);
    }

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