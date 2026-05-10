package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.GuiaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.GuiaCuidadoDTO;
import com.orquicombeima.proyecto_orquideas.model.GuiaCuidado;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.repository.GuiaCuidadoRepository;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuiaAdminService {

    private final GuiaCuidadoRepository guiaCuidadoRepository;
    private final OrquideaRepository orquideaRepository;
    private final CloudinaryService cloudinaryService;

    // GET - Listar todas las guías
    @Transactional(readOnly = true)
    public List<GuiaCuidadoDTO> listarGuias() {
        return guiaCuidadoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // POST - Crear una nueva guía de cuidado
    @Transactional
    public GuiaCuidadoDTO crearGuia(GuiaAdminDTO dto) throws IOException {
        Orquidea orquidea = orquideaRepository.findById(dto.getIdOrquidea())
                .orElseThrow(() -> new RuntimeException("Orquídea no encontrada con id: " + dto.getIdOrquidea()));

        // Una orquídea solo puede tener una guía → si ya existe, lanzamos error
        guiaCuidadoRepository.findByOrquideaId(dto.getIdOrquidea()).ifPresent(g -> {
            throw new RuntimeException("La orquídea ya tiene una guía de cuidado asociada (id guía: " + g.getId() + ")");
        });

        GuiaCuidado guia = new GuiaCuidado();
        guia.setOrquidea(orquidea);
        mapearCampos(guia, dto);

        if (dto.getImagen() != null && !dto.getImagen().isEmpty()) {
            String imagenUrl = cloudinaryService.subirImagen(dto.getImagen(), "guias");
            guia.setImageUrl(imagenUrl);
        }

        guiaCuidadoRepository.save(guia);
        return convertirADTO(guia);
    }

    // PUT - Actualizar una guía existente
    @Transactional
    public GuiaCuidadoDTO actualizarGuia(Long id, GuiaAdminDTO dto) throws IOException {
        GuiaCuidado guia = guiaCuidadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía de cuidado no encontrada con id: " + id));

        // Si el admin cambia la orquídea asociada, validamos que exista y que no esté ya tomada por otra guía
        if (!guia.getOrquidea().getId().equals(dto.getIdOrquidea())) {
            Orquidea nuevaOrquidea = orquideaRepository.findById(dto.getIdOrquidea())
                    .orElseThrow(() -> new RuntimeException("Orquídea no encontrada con id: " + dto.getIdOrquidea()));
            guiaCuidadoRepository.findByOrquideaId(dto.getIdOrquidea()).ifPresent(g -> {
                if (!g.getId().equals(id)) {
                    throw new RuntimeException("La orquídea ya tiene otra guía asociada (id guía: " + g.getId() + ")");
                }
            });
            guia.setOrquidea(nuevaOrquidea);
        }

        mapearCampos(guia, dto);

        // Si llega imagen nueva, borramos la anterior y subimos la nueva
        if (dto.getImagen() != null && !dto.getImagen().isEmpty()) {
            if (guia.getImageUrl() != null) {
                cloudinaryService.eliminarImagen(guia.getImageUrl());
            }
            String imagenUrl = cloudinaryService.subirImagen(dto.getImagen(), "guias");
            guia.setImageUrl(imagenUrl);
        }

        guiaCuidadoRepository.save(guia);
        return convertirADTO(guia);
    }

    // DELETE - Eliminar una guía (también borra la imagen de Cloudinary)
    @Transactional
    public void eliminarGuia(Long id) throws IOException {
        GuiaCuidado guia = guiaCuidadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía de cuidado no encontrada con id: " + id));

        if (guia.getImageUrl() != null) {
            cloudinaryService.eliminarImagen(guia.getImageUrl());
        }

        guiaCuidadoRepository.delete(guia);
    }

    // Copia los campos del DTO a la entidad (no toca imagen ni orquídea, eso se maneja arriba)
    private void mapearCampos(GuiaCuidado guia, GuiaAdminDTO dto) {
        guia.setTitulo(dto.getTitulo());
        guia.setVariedad(dto.getVariedad());
        guia.setContenido(dto.getContenido());
        guia.setFrecuenciaRiego(dto.getFrecuenciaRiego());
        guia.setLuzRequerida(dto.getLuzRequerida());
        guia.setTemperaturaIdeal(dto.getTemperaturaIdeal());
        guia.setFertilizacion(dto.getFertilizacion());
    }

    // Convierte la entidad a DTO público
    private GuiaCuidadoDTO convertirADTO(GuiaCuidado g) {
        return GuiaCuidadoDTO.builder()
                .id(g.getId())
                .titulo(g.getTitulo())
                .variedad(g.getVariedad())
                .contenido(g.getContenido())
                .frecuenciaRiego(g.getFrecuenciaRiego())
                .luzRequerida(g.getLuzRequerida())
                .temperaturaIdeal(g.getTemperaturaIdeal())
                .fertilizacion(g.getFertilizacion())
                .imageUrl(g.getImageUrl())
                .idOrquidea(g.getOrquidea().getId())
                .nombreOrquidea(g.getOrquidea().getNombre())
                .build();
    }
}