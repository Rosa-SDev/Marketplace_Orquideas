package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.RecomendacionAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.RecomendacionDTO;
import com.orquicombeima.proyecto_orquideas.model.Maceta;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.model.RecomendacionMaceta;
import com.orquicombeima.proyecto_orquideas.repository.MacetaRepository;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import com.orquicombeima.proyecto_orquideas.repository.RecomendacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecomendacionAdminService {

    private final RecomendacionRepository recomendacionRepository;
    private final OrquideaRepository orquideaRepository;
    private final MacetaRepository macetaRepository;

    // Metodo GET para listar todas las recomendaciones
    public List<RecomendacionDTO> listarTodas() {
        return recomendacionRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Metodo POST para crear una nueva recomendacion
    @Transactional
    public RecomendacionDTO crearRecomendacion(RecomendacionAdminDTO dto) {
        Orquidea orquidea = orquideaRepository.findById(dto.getIdOrquidea())
                .orElseThrow(() -> new RuntimeException("Orquidea no encontrada con el id: " + dto.getIdOrquidea()));

        Maceta maceta = macetaRepository.findById(dto.getIdMaceta())
                .orElseThrow(() -> new RuntimeException("Maceta no encontrada con el id: " + dto.getIdMaceta()));

        RecomendacionMaceta recomendacion = new RecomendacionMaceta();
        recomendacion.setMaceta(maceta);
        recomendacion.setOrquidea(orquidea);
        recomendacion.setDescripcion(dto.getDescripcion());

        recomendacionRepository.save(recomendacion);
        return convertirADTO(recomendacion);
    }

    // Metodo PUT para actualizar una recomendacion
    @Transactional
    public RecomendacionDTO actualizarRecomendacion(Long id, RecomendacionAdminDTO dto) {
        RecomendacionMaceta recomendacion = recomendacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recomendacion no encontrada con el id: " + id));

        Orquidea orquidea = orquideaRepository.findById(dto.getIdOrquidea())
                .orElseThrow(() -> new RuntimeException("Orquidea no encontrada con el id: " + dto.getIdOrquidea()));

        Maceta maceta = macetaRepository.findById(dto.getIdMaceta())
                .orElseThrow(() -> new RuntimeException("Maceta no encontrada con el id: " + dto.getIdMaceta()));

        recomendacion.setOrquidea(orquidea);
        recomendacion.setMaceta(maceta);
        recomendacion.setDescripcion(dto.getDescripcion());

        recomendacionRepository.save(recomendacion);
        return convertirADTO(recomendacion);
    }

    // Metodo DELETE para eliminar una recomendacion
    @Transactional
    public void eliminarRecomendacion(Long id) {
        RecomendacionMaceta recomendacion = recomendacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recomendacion no encontrada con el id: " + id));

        recomendacionRepository.delete(recomendacion);
    }

    // Función para convertir a DTO
    private RecomendacionDTO convertirADTO(RecomendacionMaceta recomendacion) {
        return RecomendacionDTO.builder()
                .id(recomendacion.getId())
                .idOrquidea(recomendacion.getOrquidea().getId())
                .nombreOrquidea(recomendacion.getOrquidea().getNombre())
                .idMaceta(recomendacion.getMaceta().getId())
                .nombreMaceta(recomendacion.getMaceta().getNombre())
                .descripcion(recomendacion.getDescripcion())
                .build();
    }

}
