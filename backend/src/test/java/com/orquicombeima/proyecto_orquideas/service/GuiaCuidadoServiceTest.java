package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.GuiaCuidadoDTO;
import com.orquicombeima.proyecto_orquideas.model.GuiaCuidado;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.repository.GuiaCuidadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuiaCuidadoServiceTest {

    @Mock private GuiaCuidadoRepository guiaCuidadoRepository;

    @InjectMocks private GuiaCuidadoService service;

    private Orquidea orquidea;
    private GuiaCuidado guia;

    @BeforeEach
    void setUp() {
        orquidea = new Orquidea();
        orquidea.setId(1L);
        orquidea.setNombre("Cattleya Trianae");

        guia = new GuiaCuidado();
        guia.setId(10L);
        guia.setOrquidea(orquidea);
        guia.setTitulo("Cuidado de la Cattleya");
        guia.setVariedad("Cattleya");
        guia.setContenido("Texto largo");
        guia.setFrecuenciaRiego("Cada 5-7 días");
        guia.setLuzRequerida("Indirecta brillante");
        guia.setTemperaturaIdeal("18-26 °C");
        guia.setFertilizacion("Mensual");
        guia.setImageUrl("https://cdn/guia.jpg");
    }

    @Test
    void obtenerTodasLasGuias_devuelveListaDeDTOsConDatosDeOrquidea() {
        when(guiaCuidadoRepository.findAll()).thenReturn(List.of(guia));

        List<GuiaCuidadoDTO> resultado = service.obtenerTodasLasGuias();

        assertThat(resultado).hasSize(1);
        GuiaCuidadoDTO dto = resultado.get(0);
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitulo()).isEqualTo("Cuidado de la Cattleya");
        assertThat(dto.getIdOrquidea()).isEqualTo(1L);
        assertThat(dto.getNombreOrquidea()).isEqualTo("Cattleya Trianae");
    }

    @Test
    void obtenerTodasLasGuias_sinResultados_devuelveListaVacia() {
        when(guiaCuidadoRepository.findAll()).thenReturn(List.of());

        List<GuiaCuidadoDTO> resultado = service.obtenerTodasLasGuias();

        assertThat(resultado).isEmpty();
    }

    @Test
    void obtenerGuiaPorVariedad_existe_devuelveDTO() {
        when(guiaCuidadoRepository.findByVariedad("Cattleya")).thenReturn(Optional.of(guia));

        GuiaCuidadoDTO dto = service.obtenerGuiaPorVariedad("Cattleya");

        assertThat(dto.getVariedad()).isEqualTo("Cattleya");
        assertThat(dto.getFrecuenciaRiego()).isEqualTo("Cada 5-7 días");
        assertThat(dto.getNombreOrquidea()).isEqualTo("Cattleya Trianae");
    }

    @Test
    void obtenerGuiaPorVariedad_noExiste_lanzaExcepcion() {
        when(guiaCuidadoRepository.findByVariedad("Phalaenopsis")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerGuiaPorVariedad("Phalaenopsis"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró una guía de cuidado para la variedad");
    }
}