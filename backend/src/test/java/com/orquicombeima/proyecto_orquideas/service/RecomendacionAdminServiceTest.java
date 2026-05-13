package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.RecomendacionAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.RecomendacionDTO;
import com.orquicombeima.proyecto_orquideas.model.Maceta;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.model.RecomendacionMaceta;
import com.orquicombeima.proyecto_orquideas.repository.MacetaRepository;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import com.orquicombeima.proyecto_orquideas.repository.RecomendacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecomendacionAdminServiceTest {

    @Mock private RecomendacionRepository recomendacionRepository;
    @Mock private OrquideaRepository orquideaRepository;
    @Mock private MacetaRepository macetaRepository;

    @InjectMocks private RecomendacionAdminService service;

    private Orquidea orquidea;
    private Maceta maceta;
    private RecomendacionMaceta recomendacion;
    private RecomendacionAdminDTO dto;

    @BeforeEach
    void setUp() {
        orquidea = new Orquidea();
        orquidea.setId(1L);
        orquidea.setNombre("Cattleya");

        maceta = new Maceta();
        maceta.setId(10L);
        maceta.setNombre("Maceta Cerámica");

        recomendacion = new RecomendacionMaceta();
        recomendacion.setId(100L);
        recomendacion.setOrquidea(orquidea);
        recomendacion.setMaceta(maceta);
        recomendacion.setDescripcion("Buena por su tamaño");

        dto = RecomendacionAdminDTO.builder()
                .idOrquidea(1L)
                .idMaceta(10L)
                .descripcion("Buena por su tamaño")
                .build();
    }

    @Test
    void listarTodas_devuelveListaDeDTOs() {
        when(recomendacionRepository.findAll()).thenReturn(List.of(recomendacion));

        List<RecomendacionDTO> resultado = service.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(100L);
        assertThat(resultado.get(0).getIdOrquidea()).isEqualTo(1L);
        assertThat(resultado.get(0).getNombreOrquidea()).isEqualTo("Cattleya");
        assertThat(resultado.get(0).getIdMaceta()).isEqualTo(10L);
        assertThat(resultado.get(0).getNombreMaceta()).isEqualTo("Maceta Cerámica");
    }

    @Test
    void crearRecomendacion_orquideaNoExiste_lanzaExcepcion() {
        when(orquideaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearRecomendacion(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Orquidea no encontrada");

        verify(recomendacionRepository, never()).save(any());
    }

    @Test
    void crearRecomendacion_macetaNoExiste_lanzaExcepcion() {
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(macetaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearRecomendacion(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Maceta no encontrada");

        verify(recomendacionRepository, never()).save(any());
    }

    @Test
    void crearRecomendacion_ok_guardaConOrquideaYMaceta() {
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(macetaRepository.findById(10L)).thenReturn(Optional.of(maceta));

        service.crearRecomendacion(dto);

        ArgumentCaptor<RecomendacionMaceta> captor = ArgumentCaptor.forClass(RecomendacionMaceta.class);
        verify(recomendacionRepository).save(captor.capture());
        RecomendacionMaceta guardada = captor.getValue();
        assertThat(guardada.getOrquidea()).isEqualTo(orquidea);
        assertThat(guardada.getMaceta()).isEqualTo(maceta);
        assertThat(guardada.getDescripcion()).isEqualTo("Buena por su tamaño");
    }

    @Test
    void actualizarRecomendacion_idNoExiste_lanzaExcepcion() {
        when(recomendacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarRecomendacion(99L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Recomendacion no encontrada");
    }

    @Test
    void actualizarRecomendacion_orquideaNoExiste_lanzaExcepcion() {
        when(recomendacionRepository.findById(100L)).thenReturn(Optional.of(recomendacion));
        when(orquideaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarRecomendacion(100L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Orquidea no encontrada");
    }

    @Test
    void actualizarRecomendacion_macetaNoExiste_lanzaExcepcion() {
        when(recomendacionRepository.findById(100L)).thenReturn(Optional.of(recomendacion));
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(macetaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarRecomendacion(100L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Maceta no encontrada");
    }

    @Test
    void actualizarRecomendacion_ok_actualizaCampos() {
        Orquidea otraOrquidea = new Orquidea();
        otraOrquidea.setId(2L);
        otraOrquidea.setNombre("Phalaenopsis");

        RecomendacionAdminDTO dtoNuevo = RecomendacionAdminDTO.builder()
                .idOrquidea(2L)
                .idMaceta(10L)
                .descripcion("Descripción actualizada")
                .build();

        when(recomendacionRepository.findById(100L)).thenReturn(Optional.of(recomendacion));
        when(orquideaRepository.findById(2L)).thenReturn(Optional.of(otraOrquidea));
        when(macetaRepository.findById(10L)).thenReturn(Optional.of(maceta));

        service.actualizarRecomendacion(100L, dtoNuevo);

        assertThat(recomendacion.getOrquidea()).isEqualTo(otraOrquidea);
        assertThat(recomendacion.getDescripcion()).isEqualTo("Descripción actualizada");
        verify(recomendacionRepository).save(recomendacion);
    }

    @Test
    void eliminarRecomendacion_idNoExiste_lanzaExcepcion() {
        when(recomendacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarRecomendacion(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Recomendacion no encontrada");
    }

    @Test
    void eliminarRecomendacion_ok_borra() {
        when(recomendacionRepository.findById(100L)).thenReturn(Optional.of(recomendacion));

        service.eliminarRecomendacion(100L);

        verify(recomendacionRepository).delete(recomendacion);
    }
}