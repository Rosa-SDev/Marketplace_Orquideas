package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.GuiaCuidadoDTO;
import com.orquicombeima.proyecto_orquideas.model.GuiaCuidado;
import com.orquicombeima.proyecto_orquideas.repository.GuiaCuidadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuiaCuidadoService {

    private final GuiaCuidadoRepository guiaCuidadoRepository;

    // GET - Obtener todas las guias de cuidado
    @Transactional(readOnly = true)     // El transactional(readOnly = true) es para que JPA pueda cargar las relaciones aunque el metodo sea de solo lectura
    public List<GuiaCuidadoDTO> obtenerTodasLasGuias() {
        return guiaCuidadoRepository.findAll().stream()
                .map(this::convertirADTO)
                .toList();
    }

    // GET - Obtener guia de cuidado por variedad
    @Transactional(readOnly = true)     // El transactional(readOnly = true) es para que JPA pueda cargar las relaciones aunque el metodo sea de solo lectura
    public GuiaCuidadoDTO obtenerGuiaPorVariedad(String variedad) {
        return guiaCuidadoRepository.findByVariedad(variedad)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RuntimeException("No se encontró una guía de cuidado para la variedad: " + variedad));
    }

    // Metodo para convertir a DTO
    private GuiaCuidadoDTO convertirADTO(GuiaCuidado guiaCuidado) {
        return GuiaCuidadoDTO.builder()
                .id(guiaCuidado.getId())
                .titulo(guiaCuidado.getTitulo())
                .variedad(guiaCuidado.getVariedad())
                .contenido(guiaCuidado.getContenido())
                .frecuenciaRiego(guiaCuidado.getFrecuenciaRiego())
                .luzRequerida(guiaCuidado.getLuzRequerida())
                .temperaturaIdeal(guiaCuidado.getTemperaturaIdeal())
                .fertilizacion(guiaCuidado.getFertilizacion())
                .imageUrl(guiaCuidado.getImageUrl())
                .idOrquidea(guiaCuidado.getOrquidea().getId())
                .nombreOrquidea(guiaCuidado.getOrquidea().getNombre())
                .build();
    }
}
