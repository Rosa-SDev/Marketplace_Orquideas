package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.MacetaDTO;
import com.orquicombeima.proyecto_orquideas.model.Maceta;
import com.orquicombeima.proyecto_orquideas.repository.MacetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// @RequiredArgsConstructor (Lombok) crea el constructor que inyecta MacetaRepository automáticamente, sin necesidad de escribir @Autowired
@Service
@RequiredArgsConstructor
public class MacetaService {

    private final MacetaRepository macetaRepository;

    // Metodo para listar macetas por estado activo
    public List<MacetaDTO> obtenerMacetasActivas() {
        return macetaRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Metodo para convertir una entidad Maceta en un MacetaDTO
    private MacetaDTO convertirADTO(Maceta maceta) {
        return MacetaDTO.builder()
                .id(maceta.getId())
                .nombre(maceta.getNombre())
                .descripcion(maceta.getDescripcion())
                .precio(maceta.getPrecio())
                .stock(maceta.getStock())
                .imageUrl(maceta.getImageUrl())
                .activo(maceta.getActivo())
                .material(maceta.getMaterial())
                .diametroCm(maceta.getDiametroCm())
                .color(maceta.getColor())
                .estilo(maceta.getEstilo())
                .build();
    }

}
