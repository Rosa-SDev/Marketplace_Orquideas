package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.MacetaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.MacetaDTO;
import com.orquicombeima.proyecto_orquideas.model.Maceta;
import com.orquicombeima.proyecto_orquideas.repository.MacetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MacetaAdminService {

    private final MacetaRepository macetaRepository;
    private final CloudinaryService cloudinaryService;

    // Metodo GET para listar todas las macetas (incluye inactivas)
    @Transactional
    public Iterable<MacetaDTO> listarMacetas() {
        return macetaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Metodo POST para crear una nueva maceta
    @Transactional
    public MacetaDTO crearMaceta(MacetaAdminDTO dto) throws IOException {
        Maceta maceta = new Maceta();
        mapearCampos(maceta, dto);

        if (dto.getImagen() != null &&  !dto.getImagen().isEmpty()) {
            String imagenUrl = cloudinaryService.subirImagen(dto.getImagen(), "macetas");
            maceta.setImageUrl(imagenUrl);
        }

        macetaRepository.save(maceta);
        return convertirADTO(maceta);
    }

    // Metodo PUT para actualizar macetas
    @Transactional
    public MacetaDTO actualizarMaceta(Long id, MacetaAdminDTO dto) throws IOException {
        Maceta maceta = macetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maceta no encontrada con id: " + id));

        mapearCampos(maceta, dto);

        if (dto.getImagen() != null &&  !dto.getImagen().isEmpty()) {
            // Si ya hay una imagen, eliminarla de Cloudinary
            if (maceta.getImageUrl() != null) {
                cloudinaryService.eliminarImagen(maceta.getImageUrl());
            }
            // Subir la nueva imagen
            String imagenUrl = cloudinaryService.subirImagen(dto.getImagen(), "macetas");
            maceta.setImageUrl(imagenUrl);
        }

        macetaRepository.save(maceta);
        return convertirADTO(maceta);
    }

    // Metodo DELETE para eliminar una maceta
    @Transactional
    public void eliminarMaceta(Long id) throws IOException {
        Maceta maceta = macetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maceta no encontrada con id: " + id));

        // En caso de que tenga imagen la maceta, eliminar tambien la imagen de cloudinary
        if (maceta.getImageUrl() != null) {
            cloudinaryService.eliminarImagen(maceta.getImageUrl());
        }

        macetaRepository.delete(maceta);
    }

    // Metodo para establecer si una maceta esta activa o no
    @Transactional
    public MacetaDTO establecerActivo(Long id) {
        Maceta maceta = macetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maceta no encontrada con id: " + id));

        maceta.setActivo(!maceta.getActivo());
        macetaRepository.save(maceta);
        return convertirADTO(maceta);
    }

    private void mapearCampos(Maceta maceta, MacetaAdminDTO  dto) {
        maceta.setNombre(dto.getNombre());
        maceta.setDescripcion(dto.getDescripcion());
        maceta.setPrecio(dto.getPrecio());
        maceta.setStock(dto.getStock());
        maceta.setMaterial(dto.getMaterial());
        maceta.setDiametroCm(dto.getDiametroCm());
        maceta.setColor(dto.getColor());
        maceta.setEstilo(dto.getEstilo());
        if (dto.getActivo() != null) {
            maceta.setActivo(dto.getActivo());
        }
    }

    private MacetaDTO convertirADTO(Maceta maceta) {
        return MacetaDTO.builder()
                .id(maceta.getId())
                .nombre(maceta.getNombre())
                .descripcion(maceta.getDescripcion())
                .precio(maceta.getPrecio())
                .stock(maceta.getStock())
                .material(maceta.getMaterial())
                .diametroCm(maceta.getDiametroCm())
                .color(maceta.getColor())
                .estilo(maceta.getEstilo())
                .activo(maceta.getActivo())
                .imageUrl(maceta.getImageUrl())
                .build();
    }
}
