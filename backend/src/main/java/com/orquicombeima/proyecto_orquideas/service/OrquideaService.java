package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.GuiaCuidadoDTO;
import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.dto.OrquideaDetalleDTO;
import com.orquicombeima.proyecto_orquideas.dto.RecomendacionMacetaDTO;
import com.orquicombeima.proyecto_orquideas.model.GuiaCuidado;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.model.RecomendacionMaceta;
import com.orquicombeima.proyecto_orquideas.repository.GuiaCuidadoRepository;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import com.orquicombeima.proyecto_orquideas.repository.RecomendacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// El controlador le pide datos a este servicio, y este se los pide al repositorio

// @Service le dice a Spring que aquí vive la lógica del negocio
// @RequiredArgsConstructor (Lombok) crea el constructor que inyecta los repositorios automáticamente
@Service
@RequiredArgsConstructor
public class OrquideaService {

    private final OrquideaRepository orquideaRepository;
    private final GuiaCuidadoRepository guiaCuidadoRepository;
    private final RecomendacionRepository recomendacionRepository;

    // Devuelve todas las orquideas como una lista de DTOs
    public List<OrquideaDTO> listarTodas() {
        List<Orquidea> orquideas = orquideaRepository.findAll();
        List<OrquideaDTO> resultado = new ArrayList<>();
        for (Orquidea o : orquideas) {
            resultado.add(convertirADTO(o));
        }
        return resultado;
    }

    // Busca una orquídea por su id y la devuelve como DTO simple
    public OrquideaDTO obtenerPorId(Long id) {
        Orquidea orquidea = orquideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró una orquídea con el id: " + id));
        return convertirADTO(orquidea);
    }

    // Busca una orquídea por id y devuelve su detalle completo: datos, guía de cuidado y recomendaciones
    public OrquideaDetalleDTO obtenerDetallePorId(Long id) {
        Orquidea orquidea = orquideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró una orquídea con el id: " + id));

        // Busca la guía de cuidado de esa orquídea (puede no tener todavía)
        GuiaCuidadoDTO guiaDTO = null;
        Optional<GuiaCuidado> guia = guiaCuidadoRepository.findByOrquideaId(id);
        if (guia.isPresent()) {
            guiaDTO = convertirGuiaADTO(guia.get());
        }

        // Busca todas las macetas recomendadas para esa orquídea
        List<RecomendacionMaceta> recomendaciones = recomendacionRepository.findByOrquideaId(id);
        List<RecomendacionMacetaDTO> recomendacionesDTO = new ArrayList<>();
        for (RecomendacionMaceta r : recomendaciones) {
            recomendacionesDTO.add(convertirRecomendacionADTO(r));
        }

        // Arma el DTO de detalle con todo junto
        OrquideaDetalleDTO detalle = new OrquideaDetalleDTO();
        detalle.setId(orquidea.getId());
        detalle.setNombre(orquidea.getNombre());
        detalle.setPrecio(orquidea.getPrecio());
        detalle.setStock(orquidea.getStock());
        detalle.setImageUrl(orquidea.getImageUrl());
        detalle.setActivo(orquidea.getActivo());
        detalle.setVariedad(orquidea.getVariedad());
        detalle.setColorFlor(orquidea.getColorFlor());
        detalle.setTamanio(orquidea.getTamanio());
        detalle.setNivelCuidado(orquidea.getNivelCuidado());
        detalle.setTiempoFloracion(orquidea.getTiempoFloracion());
        detalle.setGuiaCuidado(guiaDTO);
        detalle.setRecomendaciones(recomendacionesDTO);
        return detalle;
    }

    // Devuelve solo la lista de recomendaciones de macetas para una orquídea
    public List<RecomendacionMacetaDTO> obtenerRecomendaciones(Long id) {
        if (!orquideaRepository.existsById(id)) {
            throw new RuntimeException("No se encontró una orquídea con el id: " + id);
        }
        List<RecomendacionMaceta> recomendaciones = recomendacionRepository.findByOrquideaId(id);
        List<RecomendacionMacetaDTO> resultado = new ArrayList<>();
        for (RecomendacionMaceta r : recomendaciones) {
            resultado.add(convertirRecomendacionADTO(r));
        }
        return resultado;
    }

    // Convierte una entidad Orquidea en OrquideaDTO
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

    // Convierte una entidad GuiaCuidado en su DTO
    private GuiaCuidadoDTO convertirGuiaADTO(GuiaCuidado g) {
        GuiaCuidadoDTO dto = new GuiaCuidadoDTO();
        dto.setId(g.getId());
        dto.setTitulo(g.getTitulo());
        dto.setVariedad(g.getVariedad());
        dto.setContenido(g.getContenido());
        dto.setFrecuenciaRiego(g.getFrecuenciaRiego());
        dto.setLuzRequerida(g.getLuzRequerida());
        dto.setTemperaturaIdeal(g.getTemperaturaIdeal());
        dto.setFertilizacion(g.getFertilizacion());
        dto.setImageUrl(g.getImageUrl());
        dto.setIdOrquidea(g.getOrquidea().getId());
        dto.setNombreOrquidea(g.getOrquidea().getNombre());
        return dto;
    }

    // Convierte una entidad RecomendacionMaceta en su DTO
    private RecomendacionMacetaDTO convertirRecomendacionADTO(RecomendacionMaceta r) {
        RecomendacionMacetaDTO dto = new RecomendacionMacetaDTO();
        dto.setDescripcion(r.getDescripcion());
        dto.setMacetaId(r.getMaceta().getId());
        dto.setMacetaNombre(r.getMaceta().getNombre());
        dto.setMacetaPrecio(r.getMaceta().getPrecio());
        dto.setMacetaImageUrl(r.getMaceta().getImageUrl());
        dto.setMaterial(r.getMaceta().getMaterial());
        dto.setColor(r.getMaceta().getColor());
        dto.setEstilo(r.getMaceta().getEstilo());
        return dto;
    }
}