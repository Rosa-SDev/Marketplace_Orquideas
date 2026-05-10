package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrquideaAdminService {

    private final OrquideaRepository orquideaRepository;
    private final CloudinaryService cloudinaryService;

    // GET - Listar todas las orquideas (incluye inactivas, para que el admin las vea todas)
    @Transactional(readOnly = true)
    public List<OrquideaDTO> listarOrquideas() {
        return orquideaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // POST - Crear una nueva orquídea con su imagen
    @Transactional
    public OrquideaDTO crearOrquidea(OrquideaAdminDTO dto) throws IOException {
        Orquidea orquidea = new Orquidea();
        mapearCampos(orquidea, dto);

        if (dto.getImagen() != null && !dto.getImagen().isEmpty()) {
            String imagenUrl = cloudinaryService.subirImagen(dto.getImagen(), "orquideas");
            orquidea.setImageUrl(imagenUrl);
        }

        orquideaRepository.save(orquidea);
        return convertirADTO(orquidea);
    }

    // PUT - Actualizar una orquídea existente
    @Transactional
    public OrquideaDTO actualizarOrquidea(Long id, OrquideaAdminDTO dto) throws IOException {
        Orquidea orquidea = orquideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orquídea no encontrada con id: " + id));

        mapearCampos(orquidea, dto);

        // Si llega una imagen nueva, borramos la anterior de Cloudinary y subimos la nueva
        if (dto.getImagen() != null && !dto.getImagen().isEmpty()) {
            if (orquidea.getImageUrl() != null) {
                cloudinaryService.eliminarImagen(orquidea.getImageUrl());
            }
            String imagenUrl = cloudinaryService.subirImagen(dto.getImagen(), "orquideas");
            orquidea.setImageUrl(imagenUrl);
        }

        orquideaRepository.save(orquidea);
        return convertirADTO(orquidea);
    }

    // DELETE - Eliminar una orquídea (también borra la imagen de Cloudinary)
    @Transactional
    public void eliminarOrquidea(Long id) throws IOException {
        Orquidea orquidea = orquideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orquídea no encontrada con id: " + id));

        if (orquidea.getImageUrl() != null) {
            cloudinaryService.eliminarImagen(orquidea.getImageUrl());
        }

        orquideaRepository.delete(orquidea);
    }

    // PATCH - Alternar el estado activo/inactivo
    @Transactional
    public OrquideaDTO establecerActivo(Long id) {
        Orquidea orquidea = orquideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orquídea no encontrada con id: " + id));

        orquidea.setActivo(!orquidea.getActivo());
        orquideaRepository.save(orquidea);
        return convertirADTO(orquidea);
    }

    // Copia los campos del DTO a la entidad (no toca la imagen, eso lo manejan los métodos de arriba)
    private void mapearCampos(Orquidea orquidea, OrquideaAdminDTO dto) {
        orquidea.setNombre(dto.getNombre());
        orquidea.setDescripcion(dto.getDescripcion());
        orquidea.setPrecio(dto.getPrecio());
        orquidea.setStock(dto.getStock());
        orquidea.setVariedad(dto.getVariedad());
        orquidea.setColorFlor(dto.getColorFlor());
        orquidea.setTamanio(dto.getTamanio());
        orquidea.setNivelCuidado(dto.getNivelCuidado());
        orquidea.setTiempoFloracion(dto.getTiempoFloracion());
        if (dto.getActivo() != null) {
            orquidea.setActivo(dto.getActivo());
        }
    }

    // Convierte la entidad a DTO público
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